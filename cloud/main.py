from fastapi import FastAPI, File, UploadFile
from fastapi.responses import JSONResponse
from PIL import Image
import io
import base64
import numpy as np
from ai import zone_separate, predict, skin_analyze

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

    image_pil = Image.open(io.BytesIO(image_bytes)).convert("RGB").resize((1024,1024))
    image_np = np.array(image_pil)

    masks = zone_separate.get_zone_masks(image_np)
    cube = predict.reconstruct(image_np)
    result = skin_analyze.analyze(cube, masks, image_np)
    
    return JSONResponse(result)

