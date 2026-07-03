package com.internship.cvingestion.repository;

import java.util.UUID;

public interface CandidateSearchProjection {
    UUID getJobTrackingId();

    String getFullName();

    String getEmail();

    String getPhoneNumber();

    String getSkills();

    String getEducation();

    Double getExperienceYears();

    String getSummary();

    Double getCompatibilityScore();

    Double getVectorDistance();
}
