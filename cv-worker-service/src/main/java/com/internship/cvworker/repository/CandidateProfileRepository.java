package com.internship.cvworker.repository;

import com.internship.cvworker.model.CandidateProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CandidateProfileRepository extends JpaRepository<CandidateProfile, UUID> {

    /**
     * Performs a native pgvector Cosine Distance search.
     * '<=>' is the cosine distance operator in pgvector.
     * We convert the float[] query embedding into a vector in PostgreSQL using a cast.
     */
    @Query(value = "SELECT * FROM candidate_profiles ORDER BY embedding <=> CAST(:queryEmbedding AS vector) LIMIT :limit", 
           nativeQuery = true)
    List<CandidateProfile> findNearestCandidates(
            @Param("queryEmbedding") String queryEmbeddingString, 
            @Param("limit") int limit);
}
