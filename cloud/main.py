from fastapi import FastAPI, File, UploadFile
from fastapi.responses import JSONResponse
from PIL import Image
import io
import base64
import numpy as np
from ai import zone_separate

app = FastAPI()

@app.post("/picture")
async def skin(file: UploadFile = File(...)):
    image_bytes = await file.read()

    image_pil = Image.open(io.BytesIO(image_bytes)).convert("RGB")
    image_np = np.array(image_pil)

    masks = zone_separate.get_zone_masks(image_np)
    
    '''
    임시
    '''

    result_image, result_json = predict(image_pil, masks)

    img_buffer = io.BytesIO()
    result_image.save(img_buffer, format="JPEG")
    img_buffer.seek(0)
    encoded_image = base64.b64encode(img_buffer.read()).decode("utf-8")

    return JSONResponse({
        "image": encoded_image,
        "result": result_json
    })