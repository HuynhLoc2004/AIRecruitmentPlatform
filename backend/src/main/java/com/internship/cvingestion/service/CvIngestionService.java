package com.internship.cvingestion.service;

import com.internship.cvingestion.model.dto.JobStatusResponse;
import com.internship.cvingestion.model.dto.UploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface CvIngestionService {
    UploadResponse ingestCv(MultipartFile file, UUID jdId);
    JobStatusResponse getJobStatus(UUID trackingId);
}
