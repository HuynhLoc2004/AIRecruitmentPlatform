# AI-Powered CV Screening Platform

This repository contains the complete microservice and web application stack for an AI-Powered CV Ingestion and Semantic Search screening system. The platform is designed to efficiently process, analyze, and store candidate resumes (CVs) using modern microservices architecture, message queuing, and AI capabilities for text extraction, parsing, and embedding generation.

## ✨ Features

The platform provides the following key functionalities:

*   **Asynchronous CV Ingestion:**
    *   REST endpoint for `multipart/form-data` uploads of PDF/DOCX CV files.
    *   Files are stored securely in object storage (local filesystem mock for V1).
    *   Processing jobs are registered in a PostgreSQL database with status tracking.
    *   Asynchronous processing via RabbitMQ, ensuring high availability and resilience.
*   **Job Status & Progress Tracking:**
    *   REST endpoint to query the processing status of any uploaded CV using a `trackingId`.
    *   Provides real-time updates on job status (QUEUED, EXTRACTING, PARSING, COMPLETED, FAILED).
*   **Robust Error Handling & Resilience:**
    *   Ingestion-time validation for file format, size, and content.
    *   Processing-time failure mitigation with retries (e.g., for OCR timeouts, LLM API rate limits) and Dead-Letter Queue (DLQ) for unprocessable messages.
    *   Consumer prefetch control for backpressure management.
*   **AI-Powered Processing:**
    *   Text extraction from PDF/DOCX using Apache Tika.
    *   LLM-based parsing of raw resume text into structured fields (using Gemini 2.5 Flash via OpenAI-compatible API).
    *   Candidate embedding generation for semantic search (using Gemini `text-embedding-004`).
*   **Modern Tech Stack:**
    *   Backend services built with Spring Boot (Java 17).
    *   Frontend built with React, Vite, and Tailwind CSS.
    *   Containerization with Docker and Docker Compose.
    *   PostgreSQL with `pgvector` for efficient vector storage and search.
    *   RabbitMQ for message brokering.
    *   Redis for caching.

## 🚀 Getting Started

The entire stack is containerized and configured for rapid local execution using Docker Compose.

### Prerequisites

*   [Docker & Docker Compose](https://docs.docker.com/get-docker/)
*   [Java Development Kit (JDK) 17+](https://www.oracle.com/java/technologies/downloads/)
*   [Apache Maven](https://maven.apache.org/download.cgi) (to build the backend and worker services locally before running)
*   A `.env` file in the project root with your `GEMINI_API_KEY`.

### Environment Variables

Create a `.env` file in the root directory of the project with the following variables:

```dotenv
GEMINI_API_KEY=YOUR_GEMINI_API_KEY_HERE
# Optional: Customize RabbitMQ, PostgreSQL, Redis credentials if needed
RABBITMQ_USER=guest
RABBITMQ_PASS=guest
SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/cv_screening
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=password
REDIS_HOST=redis
REDIS_PORT=6379
```

### Running the Stack

To build the Java backend and worker binaries and spin up all containers (Frontend, Backend, Worker, RabbitMQ, Redis, PostgreSQL), run the build script in your terminal:

```bash
sh build.sh
```

On Windows PowerShell, you can run:

```powershell
cd backend; mvn clean package -DskipTests; cd ..; cd cv-worker-service; mvn clean package -DskipTests; cd ..; docker-compose up --build
```

### Access Ports

Once the containers are running, you can access the various services at:

*   **Frontend Web Dashboard:** [http://localhost:5173](http://localhost:5173) (Vite/Nginx)
*   **Backend Spring Boot API:** [http://localhost:8080](http://localhost:8080)
*   **OpenAPI Swagger Documentation:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
*   **RabbitMQ Management Dashboard:** [http://localhost:15672](http://localhost:15672) (User/Pass: `guest` / `guest`)
*   **Redis Instance:** `localhost:6379`
*   **PostgreSQL Database:** `localhost:5433`

## 🏗️ Architecture

The platform is composed of several microservices orchestrated by Docker Compose:

*   **`backend` (Spring Boot):** The CV Ingestion Service. Exposes REST APIs for file uploads and job status queries. Interacts with the database, object storage, and publishes messages to RabbitMQ.
*   **`worker` (Spring Boot):** The CV Worker Service. Consumes messages from RabbitMQ, performs text extraction (Apache Tika), LLM parsing (Gemini API), and generates embeddings. Updates job statuses and stores processed data in PostgreSQL.
*   **`frontend` (React/Vite):** A web-based dashboard for recruiters to upload CVs and monitor their processing status.
*   **`rabbitmq`:** Message broker for asynchronous communication between services.
*   **`redis`:** In-memory data store used for caching.
*   **`db` (PostgreSQL with `pgvector`):** The primary data store for job metadata, candidate profiles, and vector embeddings.

## 🛠️ Technologies Used

### Backend & Worker Services (Java/Spring Boot)

*   **Spring Boot 3.3.0:** Framework for building robust microservices.
*   **Spring Data JPA:** For database interaction with PostgreSQL.
*   **Spring AMQP:** For RabbitMQ integration.
*   **Spring Data Redis:** For Redis integration.
*   **PostgreSQL:** Relational database.
*   **pgvector:** PostgreSQL extension for vector similarity search.
*   **Apache Tika:** For text extraction from various document formats (PDF, DOCX).
*   **Lombok:** Boilerplate code reduction.
*   **dotenv-java:** For loading environment variables from `.env` file.
*   **Springdoc OpenAPI:** For generating Swagger UI documentation.

### Frontend (React)

*   **React 19:** JavaScript library for building user interfaces.
*   **Vite:** Next-generation frontend tooling for fast development.
*   **Tailwind CSS:** Utility-first CSS framework.
*   **Axios:** Promise-based HTTP client.
*   **Lucide React:** Icon library.

### Infrastructure

*   **Docker & Docker Compose:** Containerization and orchestration.
*   **RabbitMQ:** Message broker.
*   **Redis:** In-memory data store.
*   **PostgreSQL:** Relational database with vector capabilities.

## 📄 API Documentation

The Backend Spring Boot API provides interactive documentation via Swagger UI. Once the `backend` service is running, you can access it at:

[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

This documentation details the available REST endpoints, request/response schemas, and allows for direct interaction with the API.