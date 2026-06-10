import io
import base64
import numpy as np
from PIL import Image

WL = np.array([400 + 10 * i for i in range(31)], dtype=np.float32)

ZMIN = 0.5
ZMAX = 2.5
ALPHA_MAX = 0.6

# omlc 기반 흡광계수 (31밴드, 400~700nm, 10nm 간격)
EPS_MEL = np.array([2.3681, 2.1811, 2.0130, 1.8612, 1.7241, 1.5998, 1.4869, 1.3841, 1.2904, 1.2048,
                    1.1264, 1.0545, 0.9885, 0.9277, 0.8717, 0.8201, 0.7723, 0.7281, 0.6871, 0.6491,
                    0.6138, 0.5809, 0.5503, 0.5217, 0.4951, 0.4702, 0.4469, 0.4250, 0.4046, 0.3854, 0.3673],
                   dtype=np.float32)
EPS_OXY = np.array([3.7187, 6.5207, 7.3415, 4.3300, 1.4428, 0.8674, 0.6216, 0.4639, 0.4551, 0.4285,
                    0.2914, 0.2914, 0.4205, 0.5452, 0.7460, 0.6008, 0.4555, 0.6215, 0.5171, 0.2032,
                    0.0447, 0.0210, 0.0132, 0.0097, 0.0075, 0.0055, 0.0045, 0.0036, 0.0030, 0.0025, 0.0020],
                   dtype=np.float32)
EPS_DEO = np.array([4.4852, 2.0748, 1.5242, 1.4470, 1.8684, 2.3774, 2.8121, 2.5088, 1.8223, 1.5242,
                    0.4190, 0.5177, 0.7246, 0.7971, 1.0693, 0.9359, 0.9568, 0.8055, 0.6671, 0.5349,
                    0.2948, 0.1897, 0.1307, 0.1034, 0.0873, 0.0753, 0.0648, 0.0561, 0.0483, 0.0412, 0.0360],
                   dtype=np.float32)

# 기저행렬 E: [멜라닌, 옥시Hb, 디옥시Hb, 산란상수, 산란기울기] (31 x 5)
_LAM_N = (WL - WL.mean()) / WL.std()
_E = np.stack([EPS_MEL, EPS_OXY, EPS_DEO, np.ones_like(WL), _LAM_N], axis=1)
_E_PINV = np.linalg.pinv(_E)  # (5 x 31)


def _absorbance(cube: np.ndarray) -> np.ndarray:
    """반사율 cube → 흡광도. cube shape: (31, H, W), 값 범위 [0, 1]"""
    R = np.clip(cube, 1e-4, None)
    return -np.log10(R)


def compute_indices(cube: np.ndarray):
    """
    Beer-Lambert 다중회귀 언믹싱.
    cube: (31, H, W), 반사율 [0, 1]
    반환: melanin (H, W), hemoglobin (H, W)
    """
    # MST++ 출력이 (H, W, 31)이면 transpose 필요
    if cube.ndim == 3 and cube.shape[2] == 31:
        cube = cube.transpose(2, 0, 1)  # → (31, H, W)

    A = _absorbance(cube)               # (31, H, W)
    bands, H, W = A.shape
    coeff = _E_PINV @ A.reshape(bands, -1)  # (5, H*W)
    coeff = coeff.reshape(5, H, W)

    melanin   = coeff[0]                                            # a_m
    hemoglobin = np.clip(coeff[1], 0, None) + np.clip(coeff[2], 0, None)  # a_ob + a_db

    return melanin, hemoglobin


def _zscore_by_zone(index: np.ndarray, masks: dict):
    """존별로 z-score 정규화. 존 내부 픽셀끼리만 비교."""
    z = np.zeros(index.shape, dtype=np.float32)
    filled = np.zeros(index.shape, dtype=bool)
    for m in masks.values():
        mask = m.astype(bool)
        if mask.sum() < 10:
            continue
        vals = index[mask]
        mu, sd = vals.mean(), vals.std()
        z[mask] = 0.0 if sd < 1e-8 else (index[mask] - mu) / sd
        filled |= mask
    return z, filled


def _overlay(face: np.ndarray, z: np.ndarray, filled: np.ndarray,
             low_rgb, high_rgb) -> np.ndarray:
    """z-score 맵을 얼굴 이미지 위에 알파 블렌딩으로 오버레이."""
    zc = np.clip(z, -ZMAX, ZMAX)
    mag = (zc - ZMIN) / (ZMAX - ZMIN)
    mag = np.clip(mag, 0.0, 1.0)
    color = np.array(high_rgb, float)
    a = np.where(filled[..., None], (ALPHA_MAX * mag)[..., None], 0.0)
    out = face.astype(float) * (1 - a) + color * a
    return out.clip(0, 255).astype(np.uint8)


def _to_b64_png(arr: np.ndarray) -> str:
    buf = io.BytesIO()
    Image.fromarray(arr).save(buf, format="PNG")
    return base64.b64encode(buf.getvalue()).decode("ascii")


def analyze(cube: np.ndarray, masks: dict, image_np: np.ndarray) -> dict:
    """
    cube     : HSI 반사율 큐브 (31, H, W) 또는 (H, W, 31)
    masks    : 존별 boolean 마스크 dict (MediaPipe 결과)
    image_np : 시각화 배경용 RGB 이미지 (H, W, 3)
    """
    melanin, hemoglobin = compute_indices(cube)

    mel_z, filled = _zscore_by_zone(melanin, masks)
    hb_z,  _      = _zscore_by_zone(hemoglobin, masks)

    if not filled.any():
        return {"melanin_map": None, "hemoglobin_map": None}

    mel_overlay = _overlay(image_np, mel_z, filled, (40, 110, 210), (110, 60, 20))
    hb_overlay  = _overlay(image_np, hb_z,  filled, (40, 110, 210), (210, 20, 40))

    return {
        "melanin_map":   _to_b64_png(mel_overlay),
        "hemoglobin_map": _to_b64_png(hb_overlay),
    }