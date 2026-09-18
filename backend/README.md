# Spring Boot Backend + PostgreSQL

The backend exposes the resume matching API and persists every analysis to PostgreSQL.

## Endpoints

- `GET /api/health`
- `GET /api/database/health` - PostgreSQL connectivity check - backend health check
- `POST /api/match` - match text and save the result
- `POST /api/match-file` - match a PDF/DOCX/TXT resume and save the result
- `GET /api/history` - latest 50 saved analyses
- `DELETE /api/history` - clear saved analysis history

## PostgreSQL settings

Defaults are:

- Database: `resume_matcher`
- Username: `postgres`
- Password: `postgres`
- Host: `localhost`
- Port: `5432`

Environment variables can override these values:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

The first successful analysis creates/updates the `match_results` table automatically because `spring.jpa.hibernate.ddl-auto=update` is enabled.
