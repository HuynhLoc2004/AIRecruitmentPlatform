package com.internship.cvingestion.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchResponse {
    private UUID jobTrackingId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String skills;
    private String education;
    private Double experienceYears;
    private String summary;
    private Double compatibilityScore;
}
