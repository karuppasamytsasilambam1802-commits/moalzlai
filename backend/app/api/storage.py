import time
import uuid
from fastapi import APIRouter, File, UploadFile
from app.models.schemas import FileAttachmentModel

router = APIRouter()

@router.post("/storage/upload", response_model=FileAttachmentModel)
async def upload_file(file: UploadFile = File(...)):
    f_id = f"att_{int(time.time())}_{str(uuid.uuid4())[:4]}"
    file_type = "IMAGE" if file.content_type and "image" in file.content_type else "DOCUMENT"

    return FileAttachmentModel(
        id=f_id,
        fileName=file.filename or "attachment.dat",
        fileType=file_type,
        fileUrl=f"/static/uploads/{file.filename}",
        fileSize=1024
    )
