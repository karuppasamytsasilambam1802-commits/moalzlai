from fastapi import FastAPI, WebSocket, WebSocketDisconnect, Query
from fastapi.middleware.cors import CORSMiddleware
import json
import time

from app.api import rooms, translate, auth, ai, storage, web_ios_config, classes
from app.websocket.connection_manager import manager

app = FastAPI(
    title="VoxBridge AI Backend",
    description="Real-Time Multilingual Voice and Text Communication Backend Supporting Android, Web, and iOS",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(rooms.router, prefix="/api")
app.include_router(translate.router, prefix="/api")
app.include_router(auth.router, prefix="/api")
app.include_router(ai.router, prefix="/api")
app.include_router(storage.router, prefix="/api")
app.include_router(web_ios_config.router, prefix="/api")
app.include_router(classes.router, prefix="/api")

@app.get("/health")
async def health_check():
    return {
        "status": "healthy",
        "service": "VoxBridge AI FastAPI Server",
        "timestamp": int(time.time())
    }

@app.websocket("/ws/rooms/{room_id}")
async def websocket_endpoint(
    websocket: WebSocket,
    room_id: str,
    participant_id: str = Query(...),
    name: str = Query("Participant"),
    language: str = Query("en")
):
    clean_room_id = room_id.upper()
    await manager.connect(clean_room_id, participant_id, websocket)

    # Broadcast JOIN event
    join_event = {
        "type": "JOIN",
        "roomId": clean_room_id,
        "senderId": participant_id,
        "senderName": name,
        "participantObj": {
            "id": participant_id,
            "name": name,
            "preferredLanguage": language,
            "mode": "VOICE_AND_TEXT",
            "isHost": False,
            "isMuted": False,
            "isConnected": True
        },
        "timestamp": int(time.time() * 1000)
    }
    await manager.broadcast(clean_room_id, join_event)

    try:
        while True:
            data = await websocket.receive_text()
            event = json.loads(data)
            await manager.broadcast(clean_room_id, event, sender_id=participant_id)
    except WebSocketDisconnect:
        manager.disconnect(clean_room_id, participant_id)
        leave_event = {
            "type": "LEAVE",
            "roomId": clean_room_id,
            "senderId": participant_id,
            "senderName": name,
            "timestamp": int(time.time() * 1000)
        }
        await manager.broadcast(clean_room_id, leave_event)
