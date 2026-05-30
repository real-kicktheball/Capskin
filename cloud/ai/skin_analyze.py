import io
import base64
import numpy as np
from PIL import Image

WL = np.array([400 + 10 * i for i in range(31)], dtype=np.float32)
RED_IDX = np.where((WL >= 620) & (WL <= 700))[0]   # 멜라닌 영역
HB_IDX  = np.where((WL >= 540) & (WL <= 580))[0]   # 헤모글로빈 영역

ZMAX = 2.5         
ALPHA_MAX = 0.6   


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
    A = _absorbance(cube)
    slope, intercept = _fit_line(A, RED_IDX)
    melanin = -slope                                
    base = slope[None, :, :] * WL[HB_IDX][:, None, None] + intercept[None, :, :]
    hemoglobin = np.clip(A[HB_IDX] - base, 0, None).mean(axis=0)
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
    mag = np.abs(zc) / ZMAX                                 
    pos = (zc >= 0)[..., None]
    color = np.where(pos, np.array(high_rgb, float), np.array(low_rgb, float))
    a = np.where(filled[..., None], (ALPHA_MAX * mag)[..., None], 0.0)
    out = face.astype(float) * (1 - a) + color * a
    return out.clip(0, 255).astype(np.uint8)


def _to_b64_png(arr: np.ndarray) -> str:
    buf = io.BytesIO()
    Image.fromarray(arr).save(buf, format="PNG")
    return base64.b64encode(buf.getvalue()).decode("ascii")


def analyze(cube: np.ndarray, masks: dict, image_np: np.ndarray) -> dict:
    melanin, hemoglobin = compute_indices(cube)

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