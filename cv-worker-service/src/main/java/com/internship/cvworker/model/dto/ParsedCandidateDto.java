package com.internship.cvworker.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParsedCandidateDto {
    private String fullName;
    private String email;
    private String phoneNumber;
    private List<String> skills;
    private String education;
    private Double experienceYears;
    private String summary;
    private Double compatibilityScore;
}
