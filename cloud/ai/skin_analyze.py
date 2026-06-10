import io
import base64
import numpy as np
from PIL import Image

WL = np.array([400 + 10 * i for i in range(31)], dtype=np.float32)

ZMIN = 0.5
ZMAX = 2.5
ALPHA_MAX = 0.6

# OMLC/Prahl(Gratzer & Kollias) 몰흡광계수 기반, 400~700nm @10nm
# oxy/deoxy는 공통 스케일로 정규화(상대비 보존 → SpO2 유효), 멜라닌은 λ^-3.33 (mean=1)

EPS_MEL = np.array([2.3681, 2.1811, 2.0130, 1.8612, 1.7241, 1.5998, 1.4869, 1.3841, 1.2904, 1.2048,
                    1.1264, 1.0545, 0.9885, 0.9277, 0.8717, 0.8201, 0.7723, 0.7281, 0.6871, 0.6491,
                    0.6138, 0.5809, 0.5503, 0.5217, 0.4951, 0.4702, 0.4469, 0.4250, 0.4046, 0.3854, 0.3673],
                   dtype=np.float32)

EPS_OXY = np.array([3.5940, 6.3020, 6.4846, 3.3218, 1.3848, 0.8480, 0.6005, 0.4483, 0.3595, 0.3197,
                    0.2826, 0.2705, 0.3267, 0.5394, 0.7187, 0.5807, 0.4403, 0.6007, 0.6764, 0.1944,
                    0.0432, 0.0203, 0.0127, 0.0082, 0.0060, 0.0050, 0.0043, 0.0040, 0.0037, 0.0037, 0.0039],
                   dtype=np.float32)

EPS_DEO = np.array([3.0144, 4.1032, 5.5018, 7.1358, 5.5790, 1.3944, 0.3157, 0.2181, 0.1964, 0.2252,
                    0.2816, 0.3479, 0.4264, 0.5270, 0.6290, 0.7210, 0.7261, 0.6084, 0.4997, 0.3824,
                    0.1981, 0.1275, 0.0879, 0.0695, 0.0587, 0.0506, 0.0436, 0.0377, 0.0325, 0.0277, 0.0242],
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