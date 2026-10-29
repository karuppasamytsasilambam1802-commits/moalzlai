import time
import uuid
from fastapi import APIRouter, HTTPException, Query
from typing import Dict, List
from app.models.schemas import RoomCreate, RoomJoin, RoomModel, ParticipantModel, MessageModel

router = APIRouter()

# In-memory storage for rooms and messages
rooms_db: Dict[str, RoomModel] = {}
messages_db: Dict[str, List[MessageModel]] = {}

@router.post("/rooms", response_model=RoomModel)
async def create_room(req: RoomCreate):
    room_id = f"ROOM-{(1000 + len(rooms_db) + 1)}"
    host_id = str(uuid.uuid4())[:8]

    host_participant = ParticipantModel(
        id=host_id,
        name=req.hostName,
        preferredLanguage=req.preferredLanguage,
        mode=req.mode,
        isHost=True
    )

    room = RoomModel(
        roomId=room_id,
        hostId=host_id,
        hostName=req.hostName,
        supportedModes=[req.mode],
        maxParticipants=req.maxParticipants,
        participants=[host_participant],
        createdAt=int(time.time() * 1000)
    )

    rooms_db[room_id] = room
    messages_db[room_id] = []
    return room

@router.post("/rooms/join", response_model=RoomModel)
async def join_room(req: RoomJoin):
    room_id = req.roomId.upper()
    if room_id not in rooms_db:
        raise HTTPException(status_code=404, detail="Room not found")

    room = rooms_db[room_id]
    if len(room.participants) >= room.maxParticipants:
        raise HTTPException(status_code=400, detail="Room is full")

    p_id = str(uuid.uuid4())[:8]
    participant = ParticipantModel(
        id=p_id,
        name=req.participantName,
        preferredLanguage=req.preferredLanguage,
        mode=req.mode,
        isHost=False
    )

    room.participants.append(participant)
    return room

@router.get("/rooms/{room_id}", response_model=RoomModel)
async def get_room(room_id: str):
    r_id = room_id.upper()
    if r_id not in rooms_db:
        raise HTTPException(status_code=404, detail="Room not found")
    return rooms_db[r_id]

@router.get("/rooms/{room_id}/messages", response_model=List[MessageModel])
async def get_messages(room_id: str):
    r_id = room_id.upper()
    return messages_db.get(r_id, [])

@router.post("/rooms/{room_id}/leave")
async def leave_room(room_id: str, participant_id: str = Query(...)):
    r_id = room_id.upper()
    if r_id in rooms_db:
        room = rooms_db[r_id]
        room.participants = [p for p in room.participants if p.id != participant_id]
    return {"status": "success", "message": "Left room"}
