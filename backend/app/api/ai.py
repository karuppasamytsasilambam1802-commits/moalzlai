from fastapi import APIRouter
from app.models.schemas import AiSummaryRequest, AiSummaryResponse

router = APIRouter()

@router.post("/ai/summarize", response_model=AiSummaryResponse)
async def summarize_transcript(req: AiSummaryRequest):
    total = len(req.messages)
    senders = list(set([m.senderName for m in req.messages]))
    sender_str = ", ".join(senders) if senders else "Participants"

    key_points = [
        f"Key message from {m.senderName}: \"{m.originalText[:50]}\""
        for m in req.messages[:5]
    ]

    summary = f"VoxBridge AI Summarizer: Analyzed {total} multilingual messages in Room {req.roomId} with participants {sender_str}."

    answer_res = None
    if req.question:
        q = req.question.lower()
        if "who" in q:
            answer_res = f"Active room speakers: {sender_str}"
        elif "language" in q:
            langs = list(set([m.sourceLanguage.upper() for m in req.messages]))
            answer_res = f"Languages spoken in this session: {', '.join(langs)}"
        else:
            answer_res = f"AI Assistant Answer: Processed {total} conversation records."

    return AiSummaryResponse(
        summary=summary,
        keyPoints=key_points,
        sentiment="Collaborative & Multilingual",
        answer=answer_res
    )
