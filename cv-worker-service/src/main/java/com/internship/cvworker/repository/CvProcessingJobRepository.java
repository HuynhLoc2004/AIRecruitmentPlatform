package com.internship.cvworker.repository;

import com.internship.cvworker.model.CvProcessingJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CvProcessingJobRepository extends JpaRepository<CvProcessingJob, UUID> {
}
