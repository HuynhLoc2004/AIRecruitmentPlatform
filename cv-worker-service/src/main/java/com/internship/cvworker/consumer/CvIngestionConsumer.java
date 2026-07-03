package com.internship.cvworker.consumer;

import com.internship.cvworker.model.CvJobStatus;
import com.internship.cvworker.model.CvProcessingJob;
import com.internship.cvworker.model.CandidateProfile;
import com.internship.cvworker.model.dto.ParsedCandidateDto;
import com.internship.cvworker.model.dto.RabbitMQMessage;
import com.internship.cvworker.repository.CvProcessingJobRepository;
import com.internship.cvworker.repository.CandidateProfileRepository;
import com.internship.cvworker.service.EmbeddingService;
import com.internship.cvworker.service.LlmParserService;
import com.internship.cvworker.service.TextExtractionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class CvIngestionConsumer {

    private final CvProcessingJobRepository jobRepository;
    private final CandidateProfileRepository candidateRepository;
    private final TextExtractionService textExtractionService;
    private final LlmParserService llmParserService;
    private final EmbeddingService embeddingService;

    @RabbitListener(queues = "${app.rabbitmq.queue}")
    public void processCvJob(RabbitMQMessage message) {
        UUID trackingId = message.getTrackingId();
        log.info("Received CV Ingestion Job. Tracking ID: {}, File: {}", trackingId, message.getFileName());

        CvProcessingJob job = jobRepository.findById(trackingId)
                .orElseThrow(() -> new RuntimeException("Job not found in database for tracking ID: " + trackingId));

        try {
            // Step 1: Text Extraction (Apache Tika)
            updateJobStatus(job, CvJobStatus.EXTRACTING, null);
            String rawText = textExtractionService.extractText(Paths.get(message.getFileStoragePath()));

            if (rawText == null || rawText.trim().length() < 10) {
                String errorMsg = "No readable text found in the CV file. Please ensure the PDF/DOCX is not empty, not scanned, and contains selectable text.";
                updateJobStatus(job, CvJobStatus.FAILED, errorMsg);
                throw new org.springframework.amqp.AmqpRejectAndDontRequeueException(errorMsg);
            }

            // Step 2: LLM Schema Parsing & JD Scoring (Gemini)
            updateJobStatus(job, CvJobStatus.PARSING, null);
            ParsedCandidateDto parsedDto = llmParserService.parseCv(rawText, message.getJdId());

            // Step 3: Embed Candidate Summary (Gemini text-embedding-004)
            String summaryText = parsedDto.getSummary();
            if (summaryText == null || summaryText.trim().isEmpty()) {
                summaryText = "No summary available.";
                parsedDto.setSummary(summaryText);
            }
            float[] embeddingVector = embeddingService.generateEmbedding(summaryText);

            // Step 4: Map & Save Candidate Profile to PostgreSQL (pgvector)
            String skillsString = parsedDto.getSkills() != null ? String.join(", ", parsedDto.getSkills()) : "";
            CandidateProfile profile = CandidateProfile.builder()
                    .jobTrackingId(trackingId)
                    .fullName(parsedDto.getFullName() != null && !parsedDto.getFullName().trim().isEmpty() ? parsedDto.getFullName() : "Unknown Candidate")
                    .email(parsedDto.getEmail() != null ? parsedDto.getEmail() : "")
                    .phoneNumber(parsedDto.getPhoneNumber() != null ? parsedDto.getPhoneNumber() : "")
                    .skills(skillsString)
                    .education(parsedDto.getEducation() != null ? parsedDto.getEducation() : "")
                    .experienceYears(parsedDto.getExperienceYears() != null ? parsedDto.getExperienceYears() : 0.0)
                    .summary(summaryText)
                    .compatibilityScore(parsedDto.getCompatibilityScore() != null ? parsedDto.getCompatibilityScore() : 0.0)
                    .embedding(embeddingVector)
                    .build();

            candidateRepository.save(profile);
            log.info("Successfully saved candidate profile in pgvector database for tracking ID: {}", trackingId);

            // Step 5: Complete Ingestion Job
            updateJobStatus(job, CvJobStatus.COMPLETED, null);
            log.info("Job execution completed successfully. Tracking ID: {}", trackingId);

        } catch (org.springframework.amqp.AmqpRejectAndDontRequeueException e) {
            log.error("Fatal parsing task validation failure for tracking ID: {}", trackingId, e);
            throw e; // Re-throw to reject without requeuing
        } catch (Exception e) {
            log.error("Failed to process CV parsing task for tracking ID: {}", trackingId, e);
            
            // Mark job as FAILED in database
            updateJobStatus(job, CvJobStatus.FAILED, e.getMessage());

            // Re-throw exception to let RabbitMQ handle retry policies and DLQ routing
            throw new RuntimeException("CV parsing task execution failure", e);
        }
    }

    private void updateJobStatus(CvProcessingJob job, CvJobStatus status, String errorMessage) {
        job.setStatus(status);
        if (errorMessage != null) {
            // Truncate to match column limits
            String truncatedError = errorMessage.length() > 990 ? errorMessage.substring(0, 990) : errorMessage;
            job.setErrorMessage(truncatedError);
        }
        jobRepository.save(job);
        log.info("Updated Ingestion Job status to {} for Tracking ID: {}", status, job.getTrackingId());
    }
}
