# AI Resume Matcher - Phase 5 + PostgreSQL

A multi-resume job matching application using React, Spring Boot, FastAPI and PostgreSQL.

## Architecture

React/Vite frontend → Spring Boot backend → FastAPI AI service
                                  ↓
                             PostgreSQL

## Features

- Upload multiple PDF, DOCX and TXT resumes
- Compare each resume against a job description
- TF-IDF/text similarity and skill coverage scoring
- Overall match score
- Matched and missing skills
- Top 3 matching resumes and full ranking
- PostgreSQL persistence for analysis history
- Stores resume metadata, extracted text, job description, scores, skills and uploaded resume bytes
- History API and frontend history viewer

## Run PostgreSQL

From the project root:

```powershell
docker compose up -d postgres
```

Default database settings:

- Database: `resume_matcher`
- User: `postgres`
- Password: `postgres`
- Port: `5432`

See `POSTGRES_SETUP.md` for local PostgreSQL setup.

## Run services

### AI service

```powershell
cd ai-service
python -m uvicorn main:app --reload --port 8000
```

### Backend

```powershell
cd backend
mvn spring-boot:run
```

### Frontend

```powershell
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173/`.

## API

- `GET /api/health`
- `GET /api/database/health` - PostgreSQL connectivity check
- `POST /api/match`
- `POST /api/match-file`
- `GET /api/history`
- `DELETE /api/history`
