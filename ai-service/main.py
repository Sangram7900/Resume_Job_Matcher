from fastapi import FastAPI, UploadFile, File, Form, HTTPException
from pydantic import BaseModel
from pypdf import PdfReader
from docx import Document
from app.engine import match_resume

app = FastAPI(title="Resume Job Matcher AI Service", version="1.0.0")


class MatchRequest(BaseModel):
    resume_text: str
    job_description: str


def extract_text(filename: str, data: bytes) -> str:
    name = (filename or "").lower()

    if name.endswith(".pdf"):
        from io import BytesIO
        reader = PdfReader(BytesIO(data))
        return "\n".join(page.extract_text() or "" for page in reader.pages)

    if name.endswith(".docx"):
        from io import BytesIO
        doc = Document(BytesIO(data))
        return "\n".join(p.text for p in doc.paragraphs)

    if name.endswith(".txt"):
        return data.decode("utf-8", errors="ignore")

    raise HTTPException(status_code=400, detail="Only PDF, DOCX, and TXT files are supported.")


@app.get("/")
def root():
    return {"service": "resume-job-matcher-ai", "status": "running"}


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/api/ai/match")
def match_text(request: MatchRequest):
    return match_resume(request.resume_text, request.job_description)


@app.post("/api/ai/match-file")
async def match_file(
    resume: UploadFile = File(...),
    job_description: str = Form(...)
):
    data = await resume.read()
    resume_text = extract_text(resume.filename or "", data)
    return match_resume(resume_text, job_description)
