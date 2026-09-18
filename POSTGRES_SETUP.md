# PostgreSQL setup

The backend uses Spring Data JPA with the PostgreSQL JDBC driver. Hibernate automatically creates/updates the `match_results` table when the application starts.

## Docker setup

Install Docker Desktop, then from the project root run:

```powershell
docker compose up -d postgres
```

The included `docker-compose.yml` starts PostgreSQL 17 with:

- Host: `localhost`
- Port: `5432`
- Database: `resume_matcher`
- Username: `postgres`
- Password: `postgres`

## Existing local PostgreSQL

If PostgreSQL is already installed locally, create a database named `resume_matcher` and use your PostgreSQL credentials. Override the defaults with:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/resume_matcher"
$env:SPRING_DATASOURCE_USERNAME="postgres"
$env:SPRING_DATASOURCE_PASSWORD="your_password"
```

Then start Spring Boot.

## Verify persistence

After analyzing at least one resume:

```powershell
Invoke-RestMethod http://localhost:8080/api/history
```

The frontend also has a **Load from PostgreSQL** button for saved analysis history.
