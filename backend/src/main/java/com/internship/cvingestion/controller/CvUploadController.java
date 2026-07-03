package com.internship.cvingestion.controller;

import com.internship.cvingestion.model.dto.JobStatusResponse;
import com.internship.cvingestion.model.dto.UploadResponse;
import com.internship.cvingestion.service.CvIngestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cv")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "CV Ingestion & Tracking", description = "Endpoints for uploading resumes and checking their processing statuses.")
public class CvUploadController {

    private final CvIngestionService ingestionService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload candidate CV resume",
            description = "Receives a PDF/DOCX resume file and an optional Job Description ID. Stores the file, creates a job, and enqueues it in RabbitMQ asynchronously.",
            responses = {
                    @ApiResponse(
                            responseCode = "202",
                            description = "CV accepted and enqueued for processing.",
                            content = @Content(schema = @Schema(implementation = UploadResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid file type, empty file, or size limit exceeded.",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal storage or message queue publication failure.",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    )
            }
    )
    public ResponseEntity<UploadResponse> uploadCv(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "jdId", required = false) UUID jdId) {
        
        log.info("Received request to upload CV: {}. Associated JD ID: {}", file.getOriginalFilename(), jdId);
        UploadResponse response = ingestionService.ingestCv(file, jdId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/status/{trackingId}")
    @Operation(
            summary = "Get CV processing status",
            description = "Retrieves the execution status (QUEUED, EXTRACTING, PARSING, COMPLETED, FAILED) of a CV upload task.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Job status fetched successfully.",
                            content = @Content(schema = @Schema(implementation = JobStatusResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Job tracking ID not found.",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    )
            }
    )
    public ResponseEntity<JobStatusResponse> getJobStatus(
            @PathVariable @Parameter(description = "The unique tracking UUID returned upon upload.") UUID trackingId) {
        
        log.info("Received request to query status for tracking ID: {}", trackingId);
        JobStatusResponse response = ingestionService.getJobStatus(trackingId);
        return ResponseEntity.ok(response);
    }
}
