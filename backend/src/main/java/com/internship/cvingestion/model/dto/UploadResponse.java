package com.internship.cvingestion.model.dto;

import com.internship.cvingestion.model.CvJobStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadResponse {
    private UUID trackingId;
    private CvJobStatus status;
    private String fileName;
    private String fileType;
    private LocalDateTime timestamp;
}
