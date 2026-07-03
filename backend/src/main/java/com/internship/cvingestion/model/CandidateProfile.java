package com.internship.cvingestion.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "candidate_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateProfile {

    @Id
    @Column(name = "job_tracking_id")
    private UUID jobTrackingId; // Links 1-to-1 with Ingestion trackingId

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "skills", columnDefinition = "TEXT")
    private String skills; 

    @Column(name = "education", columnDefinition = "TEXT")
    private String education;

    @Column(name = "experience_years")
    private Double experienceYears;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "compatibility_score")
    private Double compatibilityScore;

    @Column(name = "embedding", columnDefinition = "vector(768)")
    private float[] embedding; // 768-dimension vector for pgvector

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
