import time
import uuid
from fastapi import APIRouter, HTTPException
from typing import Dict, List
from app.models.schemas import OnlineClassModel, HandRaiseModel

router = APIRouter()

classes_db: Dict[str, OnlineClassModel] = {}
hand_raises_db: Dict[str, List[HandRaiseModel]] = {}

@router.post("/classes", response_model=OnlineClassModel)
async def create_online_class(cls: OnlineClassModel):
    class_id = cls.classId.upper() or f"CLASS-{(1000 + len(classes_db) + 1)}"
    new_class = OnlineClassModel(
        classId=class_id,
        subjectName=cls.subjectName,
        instructorName=cls.instructorName,
        scheduledTime=cls.scheduledTime,
        attendeesCount=cls.attendeesCount,
        isLectureActive=True
    )
    classes_db[class_id] = new_class
    hand_raises_db[class_id] = []
    return new_class

@router.get("/classes/{class_id}", response_model=OnlineClassModel)
async def get_online_class(class_id: str):
    c_id = class_id.upper()
    if c_id not in classes_db:
        # Fallback default class
        return OnlineClassModel(
            classId=c_id,
            subjectName="Computer Science & Multilingual AI",
            instructorName="Prof. Alan Turing",
            scheduledTime="Live Lecture",
            attendeesCount=15,
            isLectureActive=True
        )
    return classes_db[c_id]

@router.post("/classes/{class_id}/raise-hand", response_model=HandRaiseModel)
async def raise_hand(class_id: str, hand: HandRaiseModel):
    c_id = class_id.upper()
    if c_id not in hand_raises_db:
        hand_raises_db[c_id] = []

    event = HandRaiseModel(
        studentId=hand.studentId,
        studentName=hand.studentName,
        status="RAISED"
    )
    hand_raises_db[c_id].append(event)
    return event
