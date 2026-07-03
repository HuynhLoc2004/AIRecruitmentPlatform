package com.internship.cvingestion.repository;

import com.internship.cvingestion.model.CvProcessingJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CvProcessingJobRepository extends JpaRepository<CvProcessingJob, UUID> {
}
