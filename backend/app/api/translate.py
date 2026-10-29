from fastapi import APIRouter
from app.models.schemas import TranslationRequest, TranslationResponse

router = APIRouter()

DEMO_DICTIONARY = {
    ("ta", "en"): {
        "வணக்கம்": "Hello",
        "வணக்கம், அனைவருக்கும் நல்வரவு!": "Hello everyone, welcome!",
        "நன்றி": "Thank you",
        "எப்படி இருக்கிறீர்கள்": "How are you doing today?"
    },
    ("en", "ta"): {
        "hello": "வணக்கம்",
        "hello everyone, welcome!": "வணக்கம், அனைவருக்கும் நல்வரவு!",
        "thank you": "மிக்க நன்றி",
        "how are you": "நீங்கள் எப்படி இருக்கிறீர்கள்?"
    },
    ("hi", "en"): {
        "नमस्ते": "Hello",
        "धन्यवाद": "Thank you"
    },
    ("es", "en"): {
        "hola": "Hello",
        "gracias": "Thank you"
    }
}

@router.post("/translate", response_model=TranslationResponse)
async def translate_text(req: TranslationRequest):
    translations = {}
    src = req.sourceLanguage.lower()
    text = req.text

    for target in req.targetLanguages:
        tgt = target.lower()
        if src == tgt:
            translations[target] = text
            continue

        pair_key = (src, tgt)
        matched = False
        if pair_key in DEMO_DICTIONARY:
            for phrase, translation in DEMO_DICTIONARY[pair_key].items():
                if phrase.lower() in text.lower():
                    translations[target] = translation
                    matched = True
                    break

        if not matched:
            translations[target] = f"[{target.upper()}]: {text}"

    return TranslationResponse(
        originalText=text,
        sourceLanguage=req.sourceLanguage,
        translations=translations
    )
