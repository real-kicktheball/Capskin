
import numpy as np
import torch
from pathlib import Path

from ai.MST_Plus_Plus import MST_Plus_Plus

here = Path(__file__).resolve().parent
WEIGHTS_PATH = str(here / "model.pt")

DEVICE = "cuda" if torch.cuda.is_available() else "cpu"   

_MODEL_KWARGS = dict(in_channels=3, out_channels=31, n_feat=31, stage=3)

_model = None


def load_model(weights_path: str = WEIGHTS_PATH, device: str = DEVICE):
    model = MST_Plus_Plus(**_MODEL_KWARGS)

    state_dict = torch.load(weights_path, map_location=device)
    if isinstance(state_dict, dict) and "state_dict" in state_dict:
        state_dict = state_dict["state_dict"]
    state_dict = {k.replace("module.", "", 1): v for k, v in state_dict.items()}

    missing, unexpected = model.load_state_dict(state_dict, strict=False)
    if missing or unexpected:
        print(f"[predict] load_state_dict | missing={len(missing)} unexpected={len(unexpected)}")

    model.to(device)
    model.eval()
    return model


def get_model():
    global _model
    if _model is None:
        _model = load_model()
    return _model



def _preprocess(image_np: np.ndarray) -> torch.Tensor:
    """(H,W,3) RGB -> (1,3,H,W) float32 텐서. 학습과 동일한 전체 min-max 정규화."""
    arr = np.asarray(image_np)

    if arr.ndim == 3 and arr.shape[2] > 3:
        arr = arr[:, :, :3]

    arr = arr.astype(np.float32)

    amin, amax = arr.min(), arr.max()
    denom = amax - amin
    if denom > 0:
        arr = (arr - amin) / denom
    else:
        arr = np.zeros_like(arr)  

    # HWC -> CHW -> (1,3,H,W)
    tensor = torch.from_numpy(arr.transpose(2, 0, 1)).unsqueeze(0).contiguous()
    return tensor



@torch.no_grad()
def reconstruct(image_np: np.ndarray) -> np.ndarray:

    model = get_model()

    x = _preprocess(image_np).to(DEVICE)
    pred = model(x)                     
    cube = pred.squeeze(0).cpu().numpy().astype(np.float32) 
    return cube


predict = reconstruct

if __name__ == "__main__":
    from PIL import Image, ImageDraw, ImageFont
 
    here = Path(__file__).resolve().parent
    src_path = here / "test.jpg"
 
    # 같은 폴더의 test.jpg 로드 -> RGB -> 1024x1024 리사이즈
    image_pil = Image.open(src_path).convert("RGB").resize((1024, 1024))
    image_np = np.array(image_pil)
 
    cube = reconstruct(image_np)   # (31, H, W)
    print("input", image_np.shape, "->", cube.shape, cube.dtype,
          f"range[{cube.min():.3f}, {cube.max():.3f}]")
 
    # 31개 밴드를 한 장의 그리드 이미지로 저장
    # (시각화를 위해 [0,1]로 클램프 후 0~255 스케일. 밴드 i -> 400 + 10*i nm)
    n_bands = cube.shape[0]
    cols, tile, pad, label_h = 8, 240, 6, 18           # 8열 x 4행
    rows = (n_bands + cols - 1) // cols
    cell_w, cell_h = tile + pad, tile + label_h + pad
    grid = Image.new("RGB", (cols * cell_w + pad, rows * cell_h + pad), (20, 20, 20))
    draw = ImageDraw.Draw(grid)
    try:
        font = ImageFont.truetype("DejaVuSans.ttf", 14)
    except OSError:
        font = ImageFont.load_default()
 
    for i in range(n_bands):
        band_u8 = (np.clip(cube[i], 0.0, 1.0) * 255).astype(np.uint8)
        tile_img = Image.fromarray(band_u8).resize((tile, tile))
        r, c = divmod(i, cols)
        x, y = pad + c * cell_w, pad + r * cell_h
        grid.paste(tile_img, (x, y))
        draw.text((x + 3, y + tile + 1), f"{400 + i * 10}nm", fill=(230, 230, 230), font=font)
 
    out_path = here / "bands_grid.png"
    grid.save(out_path)
    print(f"그리드 이미지 저장 완료 -> {out_path}")