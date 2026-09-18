# FastAPI AI service

Create a virtual environment and install:

```powershell
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
```

Run:

```powershell
python -m uvicorn main:app --reload --port 8000
```

Health: http://127.0.0.1:8000/health
Docs: http://127.0.0.1:8000/docs

This service intentionally uses lightweight TF-IDF/cosine similarity and does NOT require Torch or sentence-transformers.
