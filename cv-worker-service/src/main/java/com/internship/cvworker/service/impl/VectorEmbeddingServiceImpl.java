package com.internship.cvworker.service.impl;

import com.internship.cvworker.service.EmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class VectorEmbeddingServiceImpl implements EmbeddingService {
    private static final int EXPECTED_EMBEDDING_DIMENSIONS = 768;

    @Value("${app.openai.api-key}")
    private String apiKey;

    @Value("${app.openai.embedding-url}")
    private String embeddingUrl;

    @Value("${app.openai.embedding-model}")
    private String embeddingModel;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public float[] generateEmbedding(String text) {
        if (text == null || text.trim().isEmpty()) {
            log.warn("Input text for embedding is empty. Returning zero vector of dimension 768.");
            return new float[EXPECTED_EMBEDDING_DIMENSIONS];
        }
        log.info("Generating embedding for text block of length: {}", text.length());

        // Fallback for placeholder API key
        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.contains("your-api-key-here")) {
            log.warn("Gemini API Key is placeholder. Generating normalized mock vector (768 dimensions).");
            return generateMockVector(EXPECTED_EMBEDDING_DIMENSIONS);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", embeddingModel);
            requestBody.put("content", Map.of("parts", List.of(Map.of("text", text))));
            requestBody.put("output_dimensionality", EXPECTED_EMBEDDING_DIMENSIONS);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            log.info("Sending embedding request to OpenAI: {}", embeddingUrl);
            ResponseEntity<Map> response = restTemplate.postForEntity(embeddingUrl, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return parseEmbeddingResponseJson(response.getBody());
            } else {
                throw new RuntimeException("Unexpected response status from OpenAI Embeddings: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Failed to generate embedding vector via OpenAI API.", e);
            throw new RuntimeException("Embedding generation failed", e);
        }
    }

    @SuppressWarnings("unchecked")
    private float[] parseEmbeddingResponseJson(Map responseBody) {
        try {
            List<Double> embeddingDoubleList = extractEmbeddingValues(responseBody);

            float[] floatVector = new float[embeddingDoubleList.size()];
            for (int i = 0; i < embeddingDoubleList.size(); i++) {
                floatVector[i] = embeddingDoubleList.get(i).floatValue();
            }

            if (floatVector.length != EXPECTED_EMBEDDING_DIMENSIONS) {
                throw new RuntimeException("Embedding dimension mismatch. Expected "
                        + EXPECTED_EMBEDDING_DIMENSIONS + " but received " + floatVector.length);
            }

            log.info("Successfully generated embedding vector of dimension: {}", floatVector.length);
            return floatVector;
        } catch (Exception e) {
            log.error("Failed to extract embedding array from API response", e);
            throw new RuntimeException("Embedding parsing failure", e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Double> extractEmbeddingValues(Map responseBody) {
        Object embedding = responseBody.get("embedding");
        if (embedding instanceof Map embeddingMap && embeddingMap.get("values") instanceof List values) {
            return (List<Double>) values;
        }

        Object embeddings = responseBody.get("embeddings");
        if (embeddings instanceof List embeddingsList && !embeddingsList.isEmpty()) {
            Object firstEmbedding = embeddingsList.get(0);
            if (firstEmbedding instanceof Map firstEmbeddingMap && firstEmbeddingMap.get("values") instanceof List values) {
                return (List<Double>) values;
            }
        }

        Object data = responseBody.get("data");
        if (data instanceof List dataList && !dataList.isEmpty()) {
            Object firstData = dataList.get(0);
            if (firstData instanceof Map firstDataMap && firstDataMap.get("embedding") instanceof List values) {
                return (List<Double>) values;
            }
        }

        throw new RuntimeException("Embedding response did not contain embedding values.");
    }

    private float[] generateMockVector(int dimensions) {
        float[] vector = new float[dimensions];
        Random random = new Random();
        float sumOfSquares = 0.0f;

        for (int i = 0; i < dimensions; i++) {
            vector[i] = random.nextFloat() * 2.0f - 1.0f; // Range [-1.0, 1.0]
            sumOfSquares += vector[i] * vector[i];
        }

        // Normalize vector to unit length (important for cosine similarity)
        float magnitude = (float) Math.sqrt(sumOfSquares);
        for (int i = 0; i < dimensions; i++) {
            vector[i] /= magnitude;
        }

        return vector;
    }
}
