import io
import base64
import numpy as np
from PIL import Image
from sklearn.decomposition import FastICA

WL = np.array([400 + 10 * i for i in range(31)], dtype=np.float32)
RED_IDX = np.where((WL >= 620) & (WL <= 700))[0]   # 멜라닌 영역
HB_IDX  = np.where((WL >= 540) & (WL <= 580))[0]   # 헤모글로빈 영역

ZMIN = 0.5
ZMAX = 2.5         
ALPHA_MAX = 0.6   

# omlc 기반 흡광계수를 31밴드(400~700, 10nm)로 리샘플 후 평균=1로 정규화 (상대 스케일)
EPS_MEL = np.array([2.3681, 2.1811, 2.0130, 1.8612, 1.7241, 1.5998, 1.4869, 1.3841, 1.2904, 1.2048, 1.1264, 1.0545, 0.9885, 0.9277, 0.8717, 0.8201, 0.7723, 0.7281, 0.6871, 0.6491, 0.6138, 0.5809, 0.5503, 0.5217, 0.4951, 0.4702, 0.4469, 0.4250, 0.4046, 0.3854, 0.3673], dtype=np.float32)
EPS_OXY = np.array([3.7187, 6.5207, 7.3415, 4.3300, 1.4428, 0.8674, 0.6216, 0.4639, 0.4551, 0.4285, 0.2914, 0.2914, 0.4205, 0.5452, 0.7460, 0.6008, 0.4555, 0.6215, 0.5171, 0.2032, 0.0447, 0.0210, 0.0132, 0.0097, 0.0075, 0.0055, 0.0045, 0.0036, 0.0030, 0.0025, 0.0020], dtype=np.float32)
EPS_DEO = np.array([4.4852, 2.0748, 1.5242, 1.4470, 1.8684, 2.3774, 2.8121, 2.5088, 1.8223, 1.5242, 0.4190, 0.5177, 0.7246, 0.7971, 1.0693, 0.9359, 0.9568, 0.8055, 0.6671, 0.5349, 0.2948, 0.1897, 0.1307, 0.1034, 0.0873, 0.0753, 0.0648, 0.0561, 0.0483, 0.0412, 0.0360], dtype=np.float32)

# 기저행렬 E: [멜라닌, 옥시Hb, 디옥시Hb, 산란상수, 산란기울기]  (31 x 5)
_LAM_N = (WL - WL.mean()) / WL.std()                       # 산란 베이스라인용 정규화 파장
_E = np.stack([EPS_MEL, EPS_OXY, EPS_DEO, np.ones_like(WL), _LAM_N], axis=1)
_E_PINV = np.linalg.pinv(_E)                               # (5 x 31), 모든 픽셀 공통


def _absorbance(cube: np.ndarray) -> np.ndarray:
    R = np.clip(cube, 1e-4, None)
    return -np.log10(R)


def _fit_line(A: np.ndarray, band_idx: np.ndarray):
    x = WL[band_idx]
    y = A[band_idx]                 
    xc = x - x.mean()
    ybar = y.mean(axis=0)
    num = np.tensordot(xc, y - ybar, axes=(0, 0))
    slope = num / (xc ** 2).sum()
    intercept = ybar - slope * x.mean()
    return slope, intercept


def compute_indices(cube: np.ndarray):
    # Beer-Lambert 다중회귀 언믹싱: A(λ) = a_m·ε_m + a_ob·ε_ob + a_db·ε_db + 산란
    A = _absorbance(cube)                                  # (31, H, W)
    bands, H, W = A.shape
    coeff = _E_PINV @ A.reshape(bands, -1)                 # (5, H*W) 한 번에 풀기
    coeff = coeff.reshape(5, H, W)
    melanin = coeff[0]                                     # a_m
    hemoglobin = np.clip(coeff[1], 0, None) + np.clip(coeff[2], 0, None)  # a_ob + a_db
    return melanin, hemoglobin


def compute_indices_rgb(image_np: np.ndarray, skin: np.ndarray):
    """원본 RGB에서 ICA로 멜라닌·헤모글로빈 분포를 분리 (Tsumura 방식).
       OD=-log(RGB) -> 음영([1,1,1]방향) 제거 -> FastICA 2성분.
       독립 성분이라 두 맵의 상관이 0에 가까워 서로 다른 부위가 잡힘.
       반환: melanin, hemoglobin (H, W), skin 밖은 0."""
    H, W = skin.shape
    melanin = np.zeros((H, W), dtype=np.float32)
    hemoglobin = np.zeros((H, W), dtype=np.float32)
    if skin.sum() < 50:
        return melanin, hemoglobin

    rgb = np.clip(image_np.astype(np.float32) / 255.0, 1e-3, 1.0)
    OD = -np.log(rgb)                                      # (H, W, 3)
    od = OD[skin]                                          # (N, 3)

    # 음영(밝기, [1,1,1] 방향) 제거
    e = np.ones(3, dtype=np.float32) / np.sqrt(3.0)
    X = od - (od @ e)[:, None] * e[None, :]
    X = X - X.mean(0)

    try:
        ica = FastICA(n_components=2, random_state=0, max_iter=500,
                      whiten="unit-variance")
        S = ica.fit_transform(X)                          # (N, 2) 독립성분
    except Exception:
        return melanin, hemoglobin

    # 멜라닌/헤모글로빈 배정 + 부호 (OD 채널 차이 기준)
    proxy_hb  = od[:, 1] - od[:, 0]                        # 녹색 흡수 = 붉음(헤모글로빈)
    proxy_mel = od[:, 2] - od[:, 0]                        # 청색쪽 기울기 = 멜라닌
    c_hb = [abs(np.corrcoef(S[:, k], proxy_hb)[0, 1]) for k in range(2)]
    hb_idx = int(np.argmax(c_hb))
    mel_idx = 1 - hb_idx
    mel_vals, hb_vals = S[:, mel_idx].copy(), S[:, hb_idx].copy()
    if np.corrcoef(mel_vals, proxy_mel)[0, 1] < 0:
        mel_vals = -mel_vals
    if np.corrcoef(hb_vals, proxy_hb)[0, 1] < 0:
        hb_vals = -hb_vals

    melanin[skin] = mel_vals
    hemoglobin[skin] = hb_vals
    return melanin, hemoglobin


def _zscore_by_zone(index: np.ndarray, masks: dict):
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
    zc = np.clip(z, -ZMAX, ZMAX)
    # 평균보다 '높은 곳'만 칠함 (낮은 곳/파랑 제거).
    # z 가 ZMIN 이하면 안 칠하고, ZMIN~ZMAX 사이에서 알파를 0->1 로 올림
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
    # 존 마스크를 합쳐 피부 영역 만들고, 원본 RGB에서 ICA로 분리
    skin = np.zeros(image_np.shape[:2], dtype=bool)
    for m in masks.values():
        skin |= m.astype(bool)
    melanin, hemoglobin = compute_indices_rgb(image_np, skin)

    mel_z, filled = _zscore_by_zone(melanin, masks)
    hb_z,  _      = _zscore_by_zone(hemoglobin, masks)

    if not filled.any():
        return {"melanin_map": None, "hemoglobin_map": None}

    LOW = (40, 110, 210)                               
    mel_overlay = _overlay(image_np, mel_z, filled, LOW, (110, 60, 20))  
    hb_overlay  = _overlay(image_np, hb_z,  filled, LOW, (210, 20, 40))   

    return {
        "melanin_map": _to_b64_png(mel_overlay),
        "hemoglobin_map": _to_b64_png(hb_overlay),
    }