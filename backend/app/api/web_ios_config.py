from fastapi import APIRouter

router = APIRouter()

@router.get("/config/cross-platform")
async def get_cross_platform_config():
    return {
        "appName": "VoxBridge AI",
        "supportedPlatforms": ["Android", "iOS", "Web"],
        "supportedLanguages": [
            {"code": "en", "name": "English", "flag": "🇺🇸"},
            {"code": "ta", "name": "Tamil", "flag": "🇮🇳"},
            {"code": "hi", "name": "Hindi", "flag": "🇮🇳"},
            {"code": "ml", "name": "Malayalam", "flag": "🇮🇳"},
            {"code": "te", "name": "Telugu", "flag": "🇮🇳"},
            {"code": "kn", "name": "Kannada", "flag": "🇮🇳"},
            {"code": "es", "name": "Spanish", "flag": "🇪🇸"},
            {"code": "fr", "name": "French", "flag": "🇫🇷"},
            {"code": "de", "name": "German", "flag": "🇩🇪"},
            {"code": "zh", "name": "Chinese", "flag": "🇨🇳"}
        ],
        "webSocketEndpoint": "/ws/rooms/{room_id}",
        "features": {
            "speechToText": True,
            "textToSpeech": True,
            "liveCaptions": True,
            "gitHubAuth": True,
            "pdfExport": True,
            "csvExport": True,
            "aiSummarizer": True,
            "qrCodeJoin": True
        }
    }
