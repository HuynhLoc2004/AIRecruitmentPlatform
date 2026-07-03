package com.internship.cvingestion.controller;

import com.internship.cvingestion.model.dto.SearchRequest;
import com.internship.cvingestion.model.dto.SearchResponse;
import com.internship.cvingestion.repository.CandidateProfileRepository;
import com.internship.cvingestion.repository.CandidateSearchProjection;
import com.internship.cvingestion.service.EmbeddingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "CV Semantic Search", description = "Endpoints for finding candidates matching natural language descriptions.")
public class CvSearchController {

    private static final double MAX_COSINE_DISTANCE = 0.38;

    private final EmbeddingService embeddingService;
    private final CandidateProfileRepository candidateRepository;

    @PostMapping("/search")
    @Operation(
            summary = "Semantic search for candidates",
            description = "Converts query text into embeddings and runs a pgvector cosine distance search against candidate resumes.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Search completed successfully.",
                            content = @Content(schema = @Schema(implementation = SearchResponse.class))
                    )
            }
    )
    public ResponseEntity<List<SearchResponse>> searchCandidates(@RequestBody SearchRequest request) {
        String query = request.getQuery();
        int limit = request.getLimit() != null ? request.getLimit() : 10;
        
        log.info("Received request for semantic search. Query: '{}', Limit: {}", query, limit);

        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String normalizedQuery = query.trim();
        String searchableQuery = enrichSearchQuery(normalizedQuery);

        // 1. Generate Query Vector Embedding
        float[] queryVector = embeddingService.generateEmbedding(searchableQuery);

        // 2. Format Float Vector to Postgres vector string "[0.1, -0.3, ...]"
        String dbVectorString = formatVectorForPostgres(queryVector);

        // 3. Query pgvector
        List<CandidateSearchProjection> profiles = candidateRepository.findNearestCandidates(
                dbVectorString,
                normalizedQuery,
                "%" + normalizedQuery.toLowerCase() + "%",
                MAX_COSINE_DISTANCE,
                limit
        );
        log.info("Nearest candidate search returned {} matches", profiles.size());

        // 4. Map entities to response DTOs
        List<SearchResponse> responses = profiles.stream()
                .map(profile -> SearchResponse.builder()
                        .jobTrackingId(profile.getJobTrackingId())
                        .fullName(profile.getFullName())
                        .email(profile.getEmail())
                        .phoneNumber(profile.getPhoneNumber())
                        .skills(profile.getSkills())
                        .education(profile.getEducation())
                        .experienceYears(profile.getExperienceYears())
                        .summary(profile.getSummary())
                        .compatibilityScore(profile.getCompatibilityScore())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    private String formatVectorForPostgres(float[] vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            sb.append(vector[i]);
            if (i < vector.length - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private String enrichSearchQuery(String query) {
        String normalized = query.toLowerCase();
        StringBuilder enriched = new StringBuilder(query);

        if (containsAny(normalized, "front end", "frontend", "react", "tailwind", "javascript", "html", "css", "ui", "giao dien")) {
            enriched.append(" frontend React JavaScript HTML CSS Tailwind UI web interface");
        }

        if (containsAny(normalized, "back end", "backend", "spring", "java", "api", "server")) {
            enriched.append(" backend Java Spring Boot REST API PostgreSQL server");
        }

        if (containsAny(normalized, "full stack", "fullstack", "web developer", "lap trinh web", "lập trình web")) {
            enriched.append(" full stack web developer frontend backend React Java Spring Boot");
        }

        String enrichedQuery = enriched.toString();
        if (!enrichedQuery.equals(query)) {
            log.info("Enriched semantic search query from '{}' to '{}'", query, enrichedQuery);
        }
        return enrichedQuery;
    }

    private boolean containsAny(String value, String... candidates) {
        for (String candidate : candidates) {
            if (value.contains(candidate)) {
                return true;
            }
        }
        return false;
    }
}
