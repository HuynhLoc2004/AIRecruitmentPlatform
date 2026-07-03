---
title: Product Brief: AI-Powered Automated CV Screening System
status: active
created: 2026-07-01
updated: 2026-07-01
---

# Product Brief: AI-Powered Automated CV Screening System

## Executive Summary

The AI-Powered Automated CV Screening System is a highly scalable, asynchronous web application designed to automate resume ingestion, structure parsing, qualification analysis, and semantic candidate-to-job matching. By employing modern AI capabilities alongside a robust microservices architecture, the system transforms the recruitment bottleneck into a streamlined, high-throughput pipeline. 

The architecture is built upon a **React** frontend for a responsive user dashboard and a **Spring Boot** backend coordinating services. **RabbitMQ** functions as the message broker handling ingestion queues, while **PostgreSQL (pgvector)** acts as the semantic database to store and query candidate profiles. **Redis** serves as a caching layer for high-frequency assets and task states.

In recruitment, speed and accuracy are crucial. Traditional systems rely on rigid keyword searches that filter out talented candidates who use different synonyms. LLM-based parsing offers semantic understanding but is computationally expensive and slow. This product solves this trade-off by combining high-fidelity LLM parsing with an asynchronous queue-based architecture that guarantees scalability, resilience, and superior candidate matching.

## The Problem

HR teams are regularly inundated with thousands of applications for open roles. The current state of CV screening is marked by significant pain points:
* **Manual Filtering Latency:** Recruiter review is manual and slow, taking minutes per resume, causing high latency in the hiring loop.
* **Brittle Keyword Filters:** Traditional Applicant Tracking Systems (ATS) rely on simple substring matching. Qualified candidates who describe their experience with alternative terminology are missed.
* **System Strain under Spikes:** Processing a resume involves heavy operations—file type extraction, OCR, LLM analysis, and embedding generation. Under peak traffic (e.g., a major job posting launch), synchronous architectures crash or suffer API rate limit exhaustion.

## The Solution

The system introduces a decoupled, non-blocking ingestion and matching pipeline:
1. **Resume Ingestion:** Recruiters or candidates upload resumes (PDF/DOCX) via the React dashboard. The Spring Boot backend stores the file in object storage, publishes a parsing job to a RabbitMQ queue, and immediately returns a tracking ID to the frontend.
2. **Text Extraction:** An OCR worker picks up the job and extracts the plain text from the resume using Apache Tika `[ASSUMPTION: Text Extraction Library]`.
3. **Structured Ingestion & Scoring:** A worker calls an LLM (e.g., GPT-4o-mini `[ASSUMPTION: Parsing LLM]`) to extract key fields (skills, experience, projects, education) into a structured schema and calculate a compatibility score against the active Job Description (JD).
4. **Vector Generation:** The system passes candidate profile highlights through an embedding model (e.g., OpenAI `text-embedding-3-small` `[ASSUMPTION: Embedding Model]`) to produce high-dimensional candidate vectors.
5. **Database Storage:** Candidate details, scores, and embedding vectors are saved to PostgreSQL utilizing the `pgvector` extension.
6. **Fast Recall:** Recruiters can query candidate pools semantically via the dashboard. The system matches the search query vector against stored CV vectors using cosine similarity, returning candidates in real-time.

```
React Frontend ───> Spring Boot API Gateway ───> Redis (Cache / State)
                               │
                       (Publish Job)
                               │
                               ▼
                         RabbitMQ Queue
                               │
                       (Asynchronous Pull)
                               │
                               ▼
                       Parsing Workers ───> Apache Tika (OCR/Text)
                               │
                               ├───> LLM API (Structured Parse / Score)
                               │
                               ▼
                      PostgreSQL (pgvector)
```

## What Makes This Different (Scalability & Matching)

### 1. Ingestion Backpressure Management
The core engineering highlight is how the ingestion pipeline handles backpressure. LLM APIs have strict rate limits (TPM/RPM) and are slow (taking 3-10 seconds per CV). Running this synchronously would exhaust backend threads and trigger HTTP 429 (Rate Limit) errors.
* **RabbitMQ Queuing:** RabbitMQ buffers incoming files, protecting downstream LLM workers from sudden spikes.
* **Prefetch Controls:** Worker consumers are configured with a strict prefetch limit (e.g., `basic.qos(prefetch_count=2)` `[ASSUMPTION: Prefetch Count]`). This ensures a worker only pulls a new CV when it has finished processing the current one, preventing memory exhaustion.
* **Error Resilience:** Failed parses (e.g., corrupted files, API timeouts) are automatically routed to a Dead-Letter Exchange (DLX) for retry or manual inspection, ensuring no resume is ever lost.

### 2. Semantic Search over Rigid Filtering
Instead of searching for exact keywords (e.g., "ReactJS"), pgvector enables semantic searches (e.g., "frontend developer with state management experience" will retrieve candidates listing Redux, React, Vue, or Angular). High-dimensional vector comparisons run directly within the database index, keeping retrieval times low.

### 3. Redis Optimization Layer
Redis caches hot JDs and candidate search result vectors, preventing redundant pgvector calculations and lowering database load during active recruiting sessions.

## Who This Serves

* **HR Recruiters:** Need to screen through 1,000+ resumes instantly, focusing only on candidates whose profiles semantically match the JD requirements.
* **Hiring Managers:** Require a dashboard showing structured profiles, calculated match scores, and brief AI summaries explaining *why* a candidate fits the role.
* **Candidates:** Benefit from an instant, reliable application process that doesn't hang or crash during file upload.

## Success Criteria

* **Throughput Resilience:** The backend handles resume upload spikes of up to 100 CVs/second without dropping requests.
* **Processing Stability:** Zero worker crashes under sustained load; RabbitMQ successfully queues and handles backpressure to maintain LLM rate limit compliance.
* **Semantic Retrieval Speed:** Candidate search matching queries on a pool of 50,000 resumes return results in <150ms.
* **Accuracy:** AI-extracted profiles achieve >95% alignment with resume text, and matching scores correspond to human screening assessments.

## Scope

### In Scope for V1
* **File Types:** Parsing support for PDF and DOCX formats.
* **Core Pipeline:** Asynchronous parsing via RabbitMQ, Apache Tika text extraction, GPT-4o-mini parsing, and pgvector storage.
* **Semantic Search Engine:** A React interface allowing recruiters to paste a JD or type natural-language search queries, returning candidate lists sorted by relevance.
* **Redis Caching:** Caching search results, job detail entities, and processing status messages.

### Out of Scope for V1
* **ATS Integrations:** Direct API synchronization with platforms like Workday or Greenhouse.
* **Candidate Portal:** Portals allowing candidates to track application status or edit parsed profiles.
* **Multi-tenant isolation:** Multi-tenant database partitioning (V1 will assume a single-organization deployment).

## Vision

In the future, the system will evolve from a screening tool to an autonomous recruiter companion:
* **Interactive AI Screening:** Automated initial chat interactions with applicants to clarify resume gaps.
* **Autonomous Scheduling:** Auto-emailing top-matching candidates and scheduling interviews based on calendar availability.
* **Predictive Performance Models:** Utilizing historical hire performance data to retrain embedding models and optimize candidate recommendation scores.
