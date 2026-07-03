package com.internship.cvingestion.repository;

import com.internship.cvingestion.model.CandidateProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CandidateProfileRepository extends JpaRepository<CandidateProfile, UUID> {

    @Query(value = """
            WITH ranked_candidates AS (
                SELECT
                    job_tracking_id,
                    full_name,
                    email,
                    phone_number,
                    skills,
                    education,
                    experience_years,
                    summary,
                    embedding <=> CAST(:queryEmbedding AS vector) AS vector_distance,
                    ts_rank_cd(
                        setweight(to_tsvector('simple', coalesce(skills, '')), 'A') ||
                        setweight(to_tsvector('simple', coalesce(education, '')), 'A') ||
                        setweight(to_tsvector('simple', coalesce(full_name, '')), 'B') ||
                        setweight(to_tsvector('simple', coalesce(summary, '')), 'C'),
                        websearch_to_tsquery('simple', :textQuery)
                    ) AS text_rank,
                    (
                        CASE WHEN lower(coalesce(skills, '')) LIKE :likeQuery THEN 0.45 ELSE 0 END +
                        CASE WHEN lower(coalesce(education, '')) LIKE :likeQuery THEN 0.35 ELSE 0 END +
                        CASE WHEN lower(coalesce(full_name, '')) LIKE :likeQuery THEN 0.20 ELSE 0 END +
                        CASE WHEN lower(coalesce(summary, '')) LIKE :likeQuery THEN 0.15 ELSE 0 END
                    ) AS exact_score
                FROM candidate_profiles
                WHERE embedding IS NOT NULL
            ),
            scored_candidates AS (
                SELECT
                    *,
                    (
                        (GREATEST(0, 1 - vector_distance) * 0.55) +
                        (LEAST(1, text_rank) * 0.30) +
                        exact_score
                    ) AS hybrid_score
                FROM ranked_candidates
            )
            SELECT
                job_tracking_id AS "jobTrackingId",
                full_name AS "fullName",
                email AS "email",
                phone_number AS "phoneNumber",
                skills AS "skills",
                education AS "education",
                experience_years AS "experienceYears",
                summary AS "summary",
                CAST(GREATEST(0, LEAST(100, hybrid_score * 100)) AS float8) AS "compatibilityScore",
                CAST(vector_distance AS float8) AS "vectorDistance"
            FROM scored_candidates
            WHERE vector_distance <= :maxDistance
               OR text_rank > 0
               OR exact_score > 0
            ORDER BY hybrid_score DESC, vector_distance ASC
            LIMIT :limit
            """,
           nativeQuery = true)
    List<CandidateSearchProjection> findNearestCandidates(
            @Param("queryEmbedding") String queryEmbeddingString,
            @Param("textQuery") String textQuery,
            @Param("likeQuery") String likeQuery,
            @Param("maxDistance") double maxDistance,
            @Param("limit") int limit);
}
