---
title: 'Enable Real AI CV Analysis'
type: 'bugfix'
created: '2026-07-03'
status: 'done'
context:
  - '{project-root}/_bmad-output/planning-artifacts/prds/prd-Project_thucTap-2026-07-01/prd.md'
---

<frozen-after-approval reason="human-owned intent - do not modify unless human renegotiates">

## Intent

**Problem:** The CV AI pipeline exists, but a few implementation details make it hard to trust that real CV parsing and semantic search are aligned with the PRD. The embedding model differs from the PRD, Tika extraction timeout is configured but not enforced by code, and embedding dimension mismatches would fail later in pgvector with unclear symptoms.

**Approach:** Keep the current backend/worker architecture, but harden the AI path so Gemini parsing and 768-dimension embeddings run consistently when `GEMINI_API_KEY` is set. Make extraction and embedding failures explicit enough to diagnose from job status/logs.

## Boundaries & Constraints

**Always:** Preserve the existing Spring Boot services, RabbitMQ queue flow, local file storage, PostgreSQL/pgvector schema, and frontend API contract. Keep mock fallback only for missing or placeholder API keys.

**Ask First:** Do not replace Gemini, add a new OCR engine, introduce auth, or migrate the database schema beyond existing pgvector `vector(768)` expectations.

**Never:** Do not print or commit the API key. Do not remove the existing mock fallback because it is useful for local demos without a key.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Real AI path | `GEMINI_API_KEY` is set and a readable PDF/DOCX is uploaded | Worker extracts text, parses candidate JSON through Gemini, generates a 768-dimension embedding, saves `candidate_profiles`, and marks job `COMPLETED` | API or parsing failures mark job `FAILED` and allow RabbitMQ retry/DLQ policy |
| Mock path | API key is empty or placeholder | Worker keeps existing mock candidate and mock 768-dimension vector behavior | No external API call is attempted |
| Extraction hang | Tika extraction exceeds configured timeout | Job fails with a clear timeout message | Worker throws a retryable failure for RabbitMQ handling |
| Wrong vector shape | Embedding API returns a non-768 vector | Worker/search fails fast with a clear dimension mismatch | Prevents unclear pgvector insert/search errors |

</frozen-after-approval>

## Code Map

- `backend/src/main/resources/application.yml` -- backend embedding model used by semantic search queries.
- `backend/src/main/java/com/internship/cvingestion/service/impl/VectorEmbeddingServiceImpl.java` -- query embedding API integration and mock fallback.
- `cv-worker-service/src/main/resources/application.yml` -- worker parser/embedding model and extraction timeout configuration.
- `cv-worker-service/src/main/resources/tika-config.xml` -- Tika/OCR timeout baseline.
- `cv-worker-service/src/main/java/com/internship/cvworker/service/impl/TikaTextExtractionServiceImpl.java` -- CV text extraction implementation.
- `cv-worker-service/src/main/java/com/internship/cvworker/service/impl/VectorEmbeddingServiceImpl.java` -- candidate summary embedding integration and mock fallback.

## Tasks & Acceptance

**Execution:**
- [x] `backend/src/main/resources/application.yml` and `cv-worker-service/src/main/resources/application.yml` -- align embedding model to PRD `text-embedding-004`.
- [x] `cv-worker-service/src/main/resources/tika-config.xml` -- align OCR timeout value to 30 seconds.
- [x] `cv-worker-service/src/main/java/com/internship/cvworker/service/impl/TikaTextExtractionServiceImpl.java` -- enforce configured timeout around `tika.parseToString`.
- [x] Backend and worker `VectorEmbeddingServiceImpl.java` -- validate embedding response dimension equals 768.
- [x] Build backend and worker with Maven.

**Acceptance Criteria:**
- Given `GEMINI_API_KEY` is set, when the worker parses a readable uploaded CV, then it attempts real Gemini parsing and `text-embedding-004` embedding rather than mock output.
- Given extraction runs longer than `app.extraction.timeout-ms`, when the worker processes that CV, then it fails with a clear timeout error.
- Given the embedding API returns a vector whose size is not 768, when backend search or worker save generation receives it, then the service fails with an explicit dimension mismatch error.

## Spec Change Log

## Verification

**Commands:**
- `mvn -f backend/pom.xml test` -- expected: backend compiles and tests pass.
- `mvn -f cv-worker-service/pom.xml test` -- expected: worker compiles and tests pass.
