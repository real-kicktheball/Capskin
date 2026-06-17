import cv2
import mediapipe as mp
import numpy as np
from pathlib import Path
_HERE = Path(__file__).resolve().parent

BaseOptions = mp.tasks.BaseOptions
FaceLandmarker = mp.tasks.vision.FaceLandmarker
FaceLandmarkerOptions = mp.tasks.vision.FaceLandmarkerOptions
VisionRunningMode = mp.tasks.vision.RunningMode

CHEEK_LEFT  = [31, 100, 111, 116, 120, 123, 135, 138, 142, 177, 203, 206, 212, 214, 215, 216, 228, 229, 230, 231]
CHEEK_RIGHT = [261, 329, 340, 345, 349, 352, 364, 367, 371, 401, 423, 426, 432, 434, 435, 436, 448, 449, 450, 451]
MOUTH_CHIN  = [57, 140, 164, 165, 167, 169, 170, 171, 175, 186, 202, 210, 322, 369, 391, 393, 394, 395, 396, 410, 430, 432]
NOSE        = [4, 45, 115, 122, 131, 168, 174, 188, 198, 217, 220, 275, 344, 351, 360, 399, 412, 420, 437, 440]
FOREHEAD    = [8, 10, 21, 54, 55, 63, 66, 67, 71, 103, 105, 107, 109, 251, 284, 285, 293, 296, 297, 301, 332, 334, 336, 338]

LIPS          = [0, 17, 37, 39, 40, 61, 84, 91, 146, 181, 185, 267, 269, 270, 291, 314, 321, 375, 405, 409]
RIGHT_EYE     = [22, 23, 24, 25, 26, 110, 112, 130, 157, 158, 159, 160, 161, 173, 246]
LEFT_EYE      = [252, 253, 254, 255, 256, 339, 341, 359, 384, 385, 386, 387, 388, 398, 446, 466]
RIGHT_EYEBROW = [46, 52, 53, 55, 63, 65, 66, 70, 105, 107]
LEFT_EYEBROW  = [276, 282, 283, 285, 293, 295, 296, 300, 334, 336]


def get_landmarks(image_rgb, model_path="ai/face_landmarker.task"):
    H, W = image_rgb.shape[:2]

    options = FaceLandmarkerOptions(
        base_options=BaseOptions(model_asset_path=model_path),
        running_mode=VisionRunningMode.IMAGE
    )

    with FaceLandmarker.create_from_options(options) as landmarker:
        mp_image = mp.Image(image_format=mp.ImageFormat.SRGB, data=image_rgb)
        result = landmarker.detect(mp_image)
        if not result.face_landmarks:
            return None
        landmarks = result.face_landmarks[0]
        coords = [(int(lm.x * W), int(lm.y * H)) for lm in landmarks]
        return coords


def sort_clockwise(pts):

    center = pts.mean(axis=0)
    angles = np.arctan2(pts[:, 1] - center[1], pts[:, 0] - center[0])
    return pts[np.argsort(angles)]


def draw_zone_outline(image, coords, indices, color=(0, 255, 0)):
    vis = image.copy()
    for idx in indices:
        x, y = coords[idx]
        cv2.circle(vis, (x, y), 3, color, -1)
    pts = np.array([coords[i] for i in indices], dtype=np.float32)
    pts = sort_clockwise(pts).astype(np.int32)
    cv2.polylines(vis, [pts], isClosed=True, color=color, thickness=2)
    return vis


def remove_facial_features(mask, coords):

    for indices in [LEFT_EYE, RIGHT_EYE, LEFT_EYEBROW, RIGHT_EYEBROW, LIPS]:
        pts = np.array([coords[i] for i in indices], dtype=np.float32)
        pts = sort_clockwise(pts).astype(np.int32)
        center = pts.mean(axis=0)
        expanded = []
        for pt in pts:
            direction = pt - center
            norm = np.linalg.norm(direction)
            if norm > 0:
                direction = direction / norm
            expanded.append((pt + direction * 20).astype(np.int32))
        expanded = np.array(expanded, dtype=np.int32)
        cv2.fillPoly(mask, [expanded], 0)
    return mask


def get_hair_mask(image_rgb, model_path="ai/hair_segmenter.tflite"):
    ImageSegmenter = mp.tasks.vision.ImageSegmenter
    ImageSegmenterOptions = mp.tasks.vision.ImageSegmenterOptions
    options = ImageSegmenterOptions(
        base_options=BaseOptions(model_asset_path=model_path),
        output_category_mask=True
    )

    with ImageSegmenter.create_from_options(options) as segmenter:
        mp_image = mp.Image(image_format=mp.ImageFormat.SRGB, data=image_rgb)
        result = segmenter.segment(mp_image)
        category_mask = result.category_mask.numpy_view()
        hair_mask = (category_mask == 1).astype(np.uint8)

        if hair_mask.ndim == 3:
            hair_mask = hair_mask[:, :, 0]
        return hair_mask


def make_zone_mask(coords, indices, H, W, image_rgb):

    mask = np.zeros((H, W), dtype=np.uint8)
    pts = np.array([coords[i] for i in indices], dtype=np.float32)
    pts = sort_clockwise(pts).astype(np.int32)
    cv2.fillPoly(mask, [pts], 1)
    mask = remove_facial_features(mask, coords)
    hair_mask = get_hair_mask(image_rgb)
    mask[hair_mask == 1] = 0
    mask[detect_glare(image_rgb) == 1] = 0 
    return mask


def get_zone_masks(image):

    H, W = image.shape[:2]
    coords = get_landmarks(image)

    if coords is None:
        return None

    zones = {
        "cheek_left":  CHEEK_LEFT,
        "cheek_right": CHEEK_RIGHT,
        "mouth_chin":  MOUTH_CHIN,
        "nose":        NOSE,
        "forehead":    FOREHEAD,
    }

    masks = {}

    for name, indices in zones.items():
        masks[name] = make_zone_mask(coords, indices, H, W, image)
    return masks

def detect_glare(image_rgb, v_thresh=210, s_thresh=0.25):
    rgb = image_rgb.astype(np.float32)
    mx = rgb.max(axis=2)
    mn = rgb.min(axis=2)
    s = np.where(mx > 0, (mx - mn) / mx, 0.0)        # 채도
    glare = ((mx >= v_thresh) & (s <= s_thresh)).astype(np.uint8)
    glare = cv2.dilate(glare, np.ones((5, 5), np.uint8), iterations=1)  # 경계도 제외
    return glare
    
if __name__ == "__main__":
    image_rgb = cv2.cvtColor(cv2.imread("face.jpg"), cv2.COLOR_BGR2RGB)
    H, W = image_rgb.shape[:2]
    coords = get_landmarks(image_rgb)

    if coords is None:
        print("얼굴을 찾을 수 없습니다.")
    else:
        zones = {
            "cheek_left":  CHEEK_LEFT,
            "cheek_right": CHEEK_RIGHT,
            "mouth_chin":  MOUTH_CHIN,
            "nose":        NOSE,
            "forehead":    FOREHEAD,
        }
        for name, indices in zones.items():
            mask = make_zone_mask(coords, indices, H, W, image_rgb)
            vis = cv2.cvtColor(image_rgb, cv2.COLOR_RGB2BGR).copy()
            vis[mask == 0] = 0
            cv2.imwrite(f"mask_{name}.jpg", vis)
            print(f"{name} 저장 완료")