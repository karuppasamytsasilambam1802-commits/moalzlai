import json
from typing import Dict, List
from fastapi import WebSocket

class ConnectionManager:
    def __init__(self):
        # room_id -> dict of participant_id -> WebSocket
        self.active_connections: Dict[str, Dict[str, WebSocket]] = {}

    async def connect(self, room_id: str, participant_id: str, websocket: WebSocket):
        await websocket.accept()
        if room_id not in self.active_connections:
            self.active_connections[room_id] = {}
        self.active_connections[room_id][participant_id] = websocket

    def disconnect(self, room_id: str, participant_id: str):
        if room_id in self.active_connections:
            if participant_id in self.active_connections[room_id]:
                del self.active_connections[room_id][participant_id]
            if not self.active_connections[room_id]:
                del self.active_connections[room_id]

    async def broadcast(self, room_id: str, message: dict, sender_id: str = None):
        if room_id in self.active_connections:
            message_json = json.dumps(message)
            for p_id, connection in list(self.active_connections[room_id].items()):
                try:
                    await connection.send_text(message_json)
                except Exception as e:
                    print(f"Error sending message to {p_id}: {e}")

manager = ConnectionManager()
