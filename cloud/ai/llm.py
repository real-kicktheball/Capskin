# pip install google-genai python-dotenv
import os
import json
from pathlib import Path

from dotenv import load_dotenv
from google import genai
from google.genai import types

# gemini.py 옆의 .env 를 명시적으로 로드 (uvicorn 실행 위치와 무관하게 동작)
load_dotenv(Path(__file__).resolve().parent / ".env")

MODEL = "gemini-2.5-flash"

_SYSTEM_INSTRUCTION = """너는 피부 케어 안내 도우미야. 얼굴 분석 결과(JSON)만 받아서, 부위별 케어 카드를 JSON으로 생성해.

[입력 해석 규칙]
- 각 zone의 melanin_pct/hemoglobin_pct/both_pct는 "그 부위 자기 평균 대비 도드라진 면적 비율(%)"이며, 절대 심각도나 진단이 아니다.
- melanin이 뚜렷이 우세 → concern="멜라닌(색소) 경향"
- hemoglobin이 뚜렷이 우세 → concern="헤모글로빈(혈관/홍조) 경향"
- both_pct가 눈에 띄면 → concern="복합/염증 가능"
- 도드라진 게 거의 없는 zone은 카드를 만들지 마라.

[zone 한국어 매핑]
cheek_left=왼쪽 볼, cheek_right=오른쪽 볼, nose=코, forehead=이마, mouth_chin=입가·턱

[부위 특성 — care에 반영할 것]
- 이마: 피지 분비와 외부 노출이 많은 편
- 코: 피지·모공이 두드러지고 번들거리기 쉬움
- 왼쪽 볼/오른쪽 볼: 상대적으로 건조하고 색소가 자리 잡기 쉬움
- 입가·턱: 트러블과 색소가 반복되기 쉬운 부위

[concern별 권장 방향 — 구체적으로 제시]
- 멜라닌(색소): 자외선 차단 필수, 비타민C·나이아신아마이드·아젤라산 등 톤/색소 케어 성분, 강한 각질제거는 자제
- 헤모글로빈(혈관/홍조): 센텔라·판테놀·마데카소사이드 등 진정 성분, 뜨거운 물·잦은 문지름·강한 자극 줄이기, 장벽 강화
- 복합/염증: 저자극·진정 위주, 강한 액티브 성분 동시 사용 자제, 호전이 없거나 심하면 전문가 상담

[작성 규칙 — 중요]
- care는 한국어 2~3문장. "기능성 제품을 쓰세요" 같은 추상적 표현 대신 구체적인 성분이나 행동을 제시해라.
- ★ 같은 concern이라도 부위마다 care를 다르게 써라. 카드끼리 동일하거나 거의 같은 문장을 복사하는 것은 금지. 각 부위 특성을 반영해 차별화해라.
- "심한 OO", "OO질환" 같은 진단·단정 표현 금지.
- 증상이 지속·심화되면 피부과 방문을 권하는 문장을 자연스럽게 포함(모든 카드에 기계적으로 반복하진 말 것).
- 특정 브랜드명 단정 금지(성분·카테고리 수준으로만)."""

# cards 스키마 (Structured output). AI Studio export 와 동일.
_RESPONSE_SCHEMA = genai.types.Schema(
    type=genai.types.Type.OBJECT,
    required=["cards"],
    properties={
        "cards": genai.types.Schema(
            type=genai.types.Type.ARRAY,
            items=genai.types.Schema(
                type=genai.types.Type.OBJECT,
                required=["zone", "concern", "care"],
                properties={
                    "zone": genai.types.Schema(type=genai.types.Type.STRING),
                    "concern": genai.types.Schema(type=genai.types.Type.STRING),
                    "care": genai.types.Schema(type=genai.types.Type.STRING),
                },
            ),
        ),
    },
)

_CONFIG = types.GenerateContentConfig(
    temperature=0.5,
    max_output_tokens=1024,
    thinking_config=types.ThinkingConfig(thinking_budget=0),
    response_mime_type="application/json",
    response_schema=_RESPONSE_SCHEMA,
    system_instruction=[types.Part.from_text(text=_SYSTEM_INSTRUCTION)],
)

# 클라이언트는 모듈당 한 번만 생성해서 재사용 (요청마다 새로 만들지 않음)
_client = None


def _get_client():
    global _client
    if _client is None:
        api_key = os.environ.get("GEMINI_API_KEY")
        if not api_key:
            raise RuntimeError("GEMINI_API_KEY 가 없습니다. gemini.py 옆에 .env 를 만들고 키를 넣으세요.")
        _client = genai.Client(api_key=api_key)
    return _client


def generate_cards(stats: dict) -> list:
    """
    compute_zone_stats() 결과(stats dict)를 받아 Gemini로 케어 카드를 생성.
    반환: [{"zone": ..., "concern": ..., "care": ...}, ...]
    얼굴 미검출 등으로 stats 가 비어있으면 빈 리스트 반환.
    """
    if not stats:
        return []

    client = _get_client()

    contents = [
        types.Content(
            role="user",
            parts=[types.Part.from_text(text=json.dumps(stats, ensure_ascii=False))],
        ),
    ]

    response = client.models.generate_content(
        model=MODEL,
        contents=contents,
        config=_CONFIG,
    )

    try:
        data = json.loads(response.text)
        return data.get("cards", [])
    except (json.JSONDecodeError, TypeError):
        # 모델이 빈 응답/잘린 응답을 준 경우 방어
        return []


if __name__ == "__main__":
    # 단독 테스트용 샘플 (compute_zone_stats 출력 형태)
    sample = {
        "cheek_left":  {"melanin_pct": 32.3, "hemoglobin_pct": 12.0, "both_pct": 3.0, "area_px": 9000},
        "nose":        {"melanin_pct": 10.1, "hemoglobin_pct": 28.5, "both_pct": 2.0, "area_px": 3000},
        "forehead":    {"melanin_pct": 9.0,  "hemoglobin_pct": 8.0,  "both_pct": 1.0, "area_px": 12000},
    }
    cards = generate_cards(sample)
    print(json.dumps(cards, ensure_ascii=False, indent=2))