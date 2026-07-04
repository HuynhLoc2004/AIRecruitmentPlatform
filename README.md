# AI Recruitment Platform - CV Screening

AI-powered CV ingestion and candidate search platform built with Spring Boot, React, RabbitMQ, PostgreSQL/pgvector, Apache Tika, and Gemini.

The app lets recruiters upload PDF/DOCX resumes, process them asynchronously with an AI worker, store structured candidate profiles, and search candidates by natural language, skills, school, education, and related profile text.

## Current Features

- CV upload from the web UI or REST API.
- Asynchronous resume processing through RabbitMQ.
- PDF/DOCX text extraction with Apache Tika.
- Gemini-powered resume parsing into candidate profile fields.
- Gemini embedding generation with 768-dimensional vectors.
- PostgreSQL/pgvector storage for candidate embeddings.
- Hybrid candidate search:
  - semantic vector search,
  - exact matching on skills, education, name, and summary,
  - PostgreSQL full-text ranking,
  - query enrichment for short phrases such as `front end`.
- Browser voice input for search queries using the Web Speech API.
- Swagger/OpenAPI documentation for backend endpoints.
- BMAD planning artifacts for PRD, brief, and implementation specs.

## Architecture

```text
frontend React/Vite
        |
        v
backend Spring Boot API
        |
        +--> PostgreSQL/pgvector: jobs, candidate profiles, embeddings
        |
        +--> RabbitMQ: CV parsing queue
                  |
                  v
        cv-worker-service Spring Boot
                  |
                  +--> Apache Tika text extraction
                  +--> Gemini resume parsing
                  +--> Gemini embedding generation
                  +--> PostgreSQL/pgvector persistence
```

## Services

| Service | Container | Port | Purpose |
| --- | --- | --- | --- |
| Frontend | `cv-frontend` | `5173` | React UI served by Nginx |
| Backend API | `cv-backend` | `8080` | Upload, status, and search APIs |
| Worker | `cv-worker` | internal | Queue consumer for CV processing |
| RabbitMQ | `cv-rabbitmq` | `5672`, `15672` | Message broker and management UI |
| Redis | `cv-redis` | `6379` | Cache infrastructure |
| Postgres/pgvector | `cv-postgres` | `5433` | Local pgvector database fallback |

## Tech Stack

- Java 17
- Spring Boot 3.3
- Spring Data JPA
- Spring AMQP
- React 19
- Vite
- Tailwind CSS
- PostgreSQL 16 with pgvector
- RabbitMQ
- Redis
- Apache Tika
- Gemini 2.5 Flash for parsing
- Gemini Embedding API (`gemini-embedding-2`) for vector search
- Docker Compose

## Environment Setup

Create a `.env` file in the project root. Do not commit real secrets.

```dotenv
# Gemini
GEMINI_API_KEY=your_gemini_api_key_here

# Database
# Local Docker database:
SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/cv_screening
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=password

# Or an external PostgreSQL/Neon database:
# SPRING_DATASOURCE_URL=jdbc:postgresql://your-host/your-db?sslmode=require
# SPRING_DATASOURCE_USERNAME=your_user
# SPRING_DATASOURCE_PASSWORD=your_password

# RabbitMQ
RABBITMQ_USER=guest
RABBITMQ_PASS=guest

# Redis
REDIS_HOST=redis
REDIS_PORT=6379
```

A safe template is also provided in `.env.example`.

## Run With Docker Compose

```bash
docker compose up --build
```

If your Docker Compose version still uses the old command:

```bash
docker-compose up --build
```

Open the app:

- Frontend: http://localhost:5173
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- RabbitMQ UI: http://localhost:15672

Default RabbitMQ credentials are `guest` / `guest`.

## Common Development Commands

Build backend only:

```bash
docker compose build backend
```

Restart backend after code changes:

```bash
docker compose up -d backend
```

Build frontend locally:

```bash
cd frontend
npm install
npm run build
```

Run backend Maven build locally:

```bash
cd backend
mvn clean package -DskipTests
```

Run worker Maven build locally:

```bash
cd cv-worker-service
mvn clean package -DskipTests
```

## API Overview

### Upload CV

```http
POST /api/v1/cv/upload
Content-Type: multipart/form-data
```

Form field:

- `file`: PDF or DOCX resume.

Response includes a `trackingId`.

### Check Processing Status

```http
GET /api/v1/cv/status/{trackingId}
```

Typical statuses:

- `QUEUED`
- `EXTRACTING`
- `PARSING`
- `COMPLETED`
- `FAILED`

### Search Candidates

```http
POST /api/v1/search
Content-Type: application/json
```

Example:

```json
{
  "query": "front end intern React Tailwind IUH",
  "limit": 10
}
```

The search endpoint combines semantic vectors, exact text matches, and PostgreSQL full-text ranking.

## Data Flow

1. User uploads a CV from the frontend.
2. Backend stores the uploaded file and creates a processing job.
3. Backend publishes a queue message to RabbitMQ.
4. Worker consumes the message.
5. Worker extracts text with Apache Tika.
6. Worker asks Gemini to parse the resume into structured fields.
7. Worker generates an embedding for the candidate profile.
8. Worker saves the candidate profile and vector to PostgreSQL.
9. Search queries generate a query embedding and run hybrid ranking against stored profiles.

## BMAD Artifacts

This repo includes BMAD planning and implementation artifacts:

- Product brief: `_bmad-output/planning-artifacts/briefs/...`
- PRD: `_bmad-output/planning-artifacts/prds/...`
- Implementation spec: `_bmad-output/implementation-artifacts/...`

Keep these files committed so future AI/model sessions can understand the intended product direction and implementation history. Local `.memlog.md` files are ignored because they are transient working memory.

## Notes For Contributors

- Never commit `.env` or real API/database credentials.
- Use `.env.example` for safe configuration examples.
- Upload/debug resumes should not be committed.
- Generated build outputs such as `target/`, `dist/`, and `node_modules/` are ignored.
- If using an external PostgreSQL provider such as Neon, make sure the `vector` extension exists:

```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

## Known Limitations

- Candidate skills and education are currently stored as text fields. For higher precision at scale, normalize them into dedicated tables or JSON columns.
- Compatibility scores are hybrid search scores, not a formal HR assessment.
- Browser voice input depends on Web Speech API support, best tested in Chrome or Edge.
