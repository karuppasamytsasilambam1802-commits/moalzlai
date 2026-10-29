# VoxBridge AI — FastAPI Backend Server & Cross-Platform Configuration

FastAPI backend server for real-time multilingual voice & text room orchestration, WebSocket event broadcasting, translation API proxy, and Supabase / PostgreSQL database integration supporting Android, Web, and iOS clients.

## Setup & Execution Instructions

### 1. Installation
Navigate to the `backend/` directory:
```bash
cd backend
python -m venv venv
# On Windows:
venv\Scripts\activate
# On macOS/Linux:
source venv/bin/activate

pip install -r requirements.txt
```

### 2. Running the Server
Run the FastAPI application with Uvicorn bound to `0.0.0.0` so local mobile devices, web browsers, and iOS simulators can connect:
```bash
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

### 3. Database & Supabase Setup
Execute the SQL schema in `app/models/supabase_schema.sql` in your PostgreSQL / Supabase SQL Editor:
- Creates `users`, `rooms`, `messages`, `audit_logs`, and `file_attachments` tables.
- Applies Row Level Security (RLS) policies for multi-device data privacy.

### 4. Cross-Platform Endpoints Summary
- `GET /health` — Server health check
- `GET /api/config/cross-platform` — Fetch Web and iOS client capabilities
- `POST /api/rooms` — Create a new conversation room
- `POST /api/rooms/join` — Join a room by code
- `POST /api/translate` — Multilingual translation service
- `POST /api/auth/github` — GitHub OAuth authentication
- `POST /api/ai/summarize` — AI Assistant transcript summarization
- `POST /api/storage/upload` — Media & attachment storage upload
- `WS /ws/rooms/{room_id}` — Real-time WebSocket event connection across Android, iOS, and Web

### 5. Connecting Devices & Clients
- **Android Emulator**: Set server URL to `http://10.0.2.2:8000`
- **Web Browser / iOS Simulator / Physical Phone**: Set server URL to `http://<laptop-ip>:8000`
