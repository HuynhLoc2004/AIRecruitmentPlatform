package com.internship.cvingestion.service.impl;

import com.internship.cvingestion.config.StorageConfig;
import com.internship.cvingestion.exception.InvalidFileException;
import com.internship.cvingestion.exception.JobNotFoundException;
import com.internship.cvingestion.exception.StorageException;
import com.internship.cvingestion.model.CvJobStatus;
import com.internship.cvingestion.model.CvProcessingJob;
import com.internship.cvingestion.model.dto.JobStatusResponse;
import com.internship.cvingestion.model.dto.RabbitMQMessage;
import com.internship.cvingestion.model.dto.UploadResponse;
import com.internship.cvingestion.repository.CvProcessingJobRepository;
import com.internship.cvingestion.service.CvIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CvIngestionServiceImpl implements CvIngestionService {

    private final CvProcessingJobRepository jobRepository;
    private final RabbitTemplate rabbitTemplate;
    private final StorageConfig storageConfig;

    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.routing-key}")
    private String routingKey;

    @Override
    @Transactional
    public UploadResponse ingestCv(MultipartFile file, UUID jdId) {
        // 1. File Validation
        String originalFilename = file.getOriginalFilename();
        if (file.isEmpty() || originalFilename == null) {
            throw new InvalidFileException("Uploaded file cannot be empty.");
        }

        String fileExtension = getFileExtension(originalFilename);
        if (!fileExtension.equalsIgnoreCase("pdf") && !fileExtension.equalsIgnoreCase("docx")) {
            throw new InvalidFileException("Unsupported file type. Only PDF and DOCX files are allowed.");
        }

        // 2. Setup ID & Storage Paths
        UUID trackingId = UUID.randomUUID();
        String storedFilename = trackingId + "_" + originalFilename;
        Path destination = Paths.get(storageConfig.getUploadDir()).resolve(storedFilename).normalize();

        // 3. Save File Content to Local Storage
        try {
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            log.info("Saved resume file locally: {}", destination.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to store file {}", originalFilename, e);
            throw new StorageException("Could not store file on server.", e);
        }

        // 4. Create and Save Job Entity to Database (Transactional)
        CvProcessingJob job = CvProcessingJob.builder()
                .trackingId(trackingId)
                .fileName(originalFilename)
                .fileType(fileExtension.toLowerCase())
                .fileStoragePath(destination.toAbsolutePath().toString())
                .jdId(jdId)
                .status(CvJobStatus.QUEUED)
                .build();

        jobRepository.save(job);
        log.info("Registered CV Ingestion Job in Database. TrackingId: {}", trackingId);

        // 5. Publish Message to RabbitMQ
        RabbitMQMessage message = RabbitMQMessage.builder()
                .trackingId(trackingId)
                .fileName(originalFilename)
                .fileType(fileExtension.toLowerCase())
                .fileStoragePath(destination.toAbsolutePath().toString())
                .jdId(jdId)
                .timestamp(LocalDateTime.now())
                .build();

        try {
            rabbitTemplate.convertAndSend(exchangeName, routingKey, message);
            log.info("Published parsing job to RabbitMQ exchange {} with routing key {}. TrackingId: {}", 
                    exchangeName, routingKey, trackingId);
        } catch (Exception e) {
            log.error("Failed to publish message to RabbitMQ for tracking ID: {}", trackingId, e);
            // Exception will trigger rollback of transaction (DB job record won't be saved)
            // Clean up the copied file to prevent orphan files
            try {
                Files.deleteIfExists(destination);
            } catch (IOException cleanupEx) {
                log.error("Failed to delete local file on rollback: {}", destination, cleanupEx);
            }
            throw new RuntimeException("Messaging gateway failure, enqueuing aborted.", e);
        }

        // 6. Return standard response
        return UploadResponse.builder()
                .trackingId(trackingId)
                .status(CvJobStatus.QUEUED)
                .fileName(originalFilename)
                .fileType(fileExtension.toLowerCase())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    public JobStatusResponse getJobStatus(UUID trackingId) {
        CvProcessingJob job = jobRepository.findById(trackingId)
                .orElseThrow(() -> new JobNotFoundException("Job with tracking ID " + trackingId + " does not exist."));

        return JobStatusResponse.builder()
                .trackingId(job.getTrackingId())
                .status(job.getStatus())
                .error(job.getErrorMessage())
                .updatedAt(job.getUpdatedAt())
                .build();
    }

    private String getFileExtension(String filename) {
        int lastIndex = filename.lastIndexOf('.');
        if (lastIndex == -1) {
            return "";
        }
        return filename.substring(lastIndex + 1);
    }
}
