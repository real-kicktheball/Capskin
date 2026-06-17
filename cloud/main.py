from fastapi import FastAPI, File, UploadFile
from fastapi.responses import JSONResponse
from PIL import Image
import io
import base64
import numpy as np
from ai import zone_separate, predict, skin_analyze, llm

app = FastAPI()

@app.get("/health")
def health():
    return {"status": "ok"}

@app.get("/")
def root():
    return {"status": "ok"}

@app.post("/picture")
async def skin(file: UploadFile = File(...)):
    image_bytes = await file.read()

    image_pil = Image.open(io.BytesIO(image_bytes)).convert("RGB").resize((1024, 1024))
    image_np = np.array(image_pil)

    masks = zone_separate.get_zone_masks(image_np)
    if masks is None:
        return JSONResponse(
            {"error": "no_face", "message": "얼굴을 찾을 수 없습니다."},
            status_code=422,
        )

    cube = predict.reconstruct(image_np)

    result = skin_analyze.analyze(cube, masks, image_np)

    stats = skin_analyze.compute_zone_stats(cube, masks)
    try:
        cards = llm.generate_cards(stats)
    except Exception as e:
        print(f"[main] generate_cards 실패: {e}")
        cards = []

    result["cards"] = cards
    return JSONResponse(result)