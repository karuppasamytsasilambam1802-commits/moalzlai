from pydantic import BaseModel, Field
from typing import List, Dict, Optional

class ParticipantModel(BaseModel):
    id: str
    name: str
    preferredLanguage: str = "en"
    mode: str = "VOICE_AND_TEXT"
    isHost: bool = False
    isMuted: bool = False
    isConnected: bool = True

class MessageModel(BaseModel):
    id: str
    roomId: str
    senderId: str
    senderName: str
    originalText: str
    sourceLanguage: str
    translations: Dict[str, str] = Field(default_factory=dict)
    sequenceNumber: int = 0
    timestamp: int = 0
    isFinal: bool = True
    status: str = "SENT"
    isTyped: bool = True

class RoomModel(BaseModel):
    roomId: str
    hostId: str
    hostName: str
    supportedModes: List[str] = ["VOICE_AND_TEXT"]
    maxParticipants: int = 10
    participants: List[ParticipantModel] = []
    createdAt: int = 0

class RoomCreate(BaseModel):
    hostName: str
    preferredLanguage: str = "en"
    mode: str = "VOICE_AND_TEXT"
    maxParticipants: int = 10

class RoomJoin(BaseModel):
    roomId: str
    participantName: str
    preferredLanguage: str = "en"
    mode: str = "VOICE_AND_TEXT"

class TranslationRequest(BaseModel):
    text: str
    sourceLanguage: str
    targetLanguages: List[str]

class TranslationResponse(BaseModel):
    originalText: str
    sourceLanguage: str
    translations: Dict[str, str]

class GitHubUserModel(BaseModel):
    id: int = 100001
    login: str = "octocat"
    name: str = "The Octocat"
    avatarUrl: str = "https://avatars.githubusercontent.com/u/583231?v=4"
    email: Optional[str] = "octocat@github.com"
    bio: Optional[str] = "Building VoxBridge AI with Jetpack Compose & FastAPI"
    htmlUrl: Optional[str] = "https://github.com/octocat"

class GitHubAuthRequest(BaseModel):
    code: Optional[str] = None
    username: Optional[str] = None

class GitHubAuthResponse(BaseModel):
    success: bool = True
    message: str = "Authenticated with GitHub"
    user: Optional[GitHubUserModel] = None

class AiSummaryRequest(BaseModel):
    roomId: str
    messages: List[MessageModel] = []
    question: Optional[str] = None

class AiSummaryResponse(BaseModel):
    summary: str
    keyPoints: List[str] = []
    sentiment: str = "Positive"
    answer: Optional[str] = None

class FileAttachmentModel(BaseModel):
    id: str
    fileName: str
    fileType: str
    fileUrl: str
    fileSize: int

class OnlineClassModel(BaseModel):
    classId: str
    subjectName: str
    instructorName: str
    scheduledTime: str = "Live Lecture"
    attendeesCount: int = 1
    isLectureActive: bool = True

class HandRaiseModel(BaseModel):
    studentId: str
    studentName: str
    status: str = "RAISED"
