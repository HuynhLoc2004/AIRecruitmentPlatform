package com.internship.cvworker.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cv_processing_jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CvProcessingJob {

    @Id
    @Column(name = "tracking_id", updatable = false, nullable = false)
    private UUID trackingId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_type", nullable = false)
    private String fileType;

    @Column(name = "file_storage_path", nullable = false)
    private String fileStoragePath;

    @Column(name = "jd_id")
    private UUID jdId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CvJobStatus status;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
