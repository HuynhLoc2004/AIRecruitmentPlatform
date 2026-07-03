package com.internship.cvworker.service.impl;

import com.internship.cvworker.model.dto.ParsedCandidateDto;
import com.internship.cvworker.service.LlmParserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GptParserServiceImpl implements LlmParserService {

    @Value("${app.openai.api-key}")
    private String apiKey;

    @Value("${app.openai.chat-url}")
    private String chatUrl;

    @Value("${app.openai.chat-model}")
    private String modelName;

    private final RestTemplate restTemplate = new RestTemplate();
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper()
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    public ParsedCandidateDto parseCv(String resumeText, UUID jdId) {
        log.info("Requesting LLM parsing for resume text. Character length: {}, Associated JD: {}", 
                resumeText.length(), jdId);

        // Fallback for placeholder API key
        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.contains("your-api-key-here")) {
            log.warn("OpenAI/Gemini API Key is placeholder. Falling back to mock structured parsing response.");
            return generateMockParsedCandidate(jdId);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            // Construct Prompt specifying exact DTO keys for Gemini
            String systemPrompt = "You are an expert recruitment parser. Extract structured information from the candidate's resume raw text."
                    + " Return candidate name, contact, skills, education, experience details, and calculate a candidate-to-job match compatibility score (0-100) if a Job Description ID is associated."
                    + " Return ONLY a valid JSON object matching the following schema. Do not enclose in markdown blocks. Keys must be exactly:\n"
                    + " - fullName (string)\n"
                    + " - email (string)\n"
                    + " - phoneNumber (string)\n"
                    + " - skills (array of strings)\n"
                    + " - education (string)\n"
                    + " - experienceYears (number, e.g. 2.5)\n"
                    + " - summary (string, candidate summary)\n"
                    + " - compatibilityScore (number, 0 to 100)";

            String userContent = "Resume raw text:\n" + resumeText + "\n\nJob Description ID (for score context): " + (jdId != null ? jdId.toString() : "None");

            // Build request body matching Chat Completion API
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelName);
            
            // Force JSON output
            Map<String, String> responseFormat = new HashMap<>();
            responseFormat.put("type", "json_object");
            requestBody.put("response_format", responseFormat);

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));
            messages.add(Map.of("role", "user", "content", userContent));
            requestBody.put("messages", messages);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            log.info("Sending request to OpenAI/Gemini API: {}", chatUrl);
            ResponseEntity<Map> response = restTemplate.postForEntity(chatUrl, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return parseLlmResponseJson(response.getBody());
            } else {
                throw new RuntimeException("Unexpected response status from OpenAI/Gemini: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("LLM parser failed to communicate with OpenAI/Gemini API.", e);
            throw new RuntimeException("LLM parsing failure", e);
        }
    }

    @SuppressWarnings("unchecked")
    private ParsedCandidateDto parseLlmResponseJson(Map responseBody) {
        try {
            List<Map> choices = (List<Map>) responseBody.get("choices");
            Map message = (Map) choices.get(0).get("message");
            String jsonContent = (String) message.get("content");

            log.info("Received structured JSON output from LLM: {}", jsonContent);

            return mapJsonToDto(jsonContent);
        } catch (Exception e) {
            log.error("Failed to parse LLM JSON response payload", e);
            throw new RuntimeException("Failed to decode LLM response schema", e);
        }
    }

    private ParsedCandidateDto mapJsonToDto(String json) {
        log.debug("Mapping json string schema: {}", json);
        try {
            String cleanJson = json.trim();
            // Handle markdown block wrapper (e.g. ```json ... ```)
            if (cleanJson.startsWith("```")) {
                int firstNewLine = cleanJson.indexOf('\n');
                int lastBackticks = cleanJson.lastIndexOf("```");
                if (firstNewLine != -1 && lastBackticks > firstNewLine) {
                    cleanJson = cleanJson.substring(firstNewLine + 1, lastBackticks).trim();
                }
            }
            return objectMapper.readValue(cleanJson, ParsedCandidateDto.class);
        } catch (Exception e) {
            log.error("Failed to deserialize LLM JSON output", e);
            throw new RuntimeException("JSON deserialization failed", e);
        }
    }

    private ParsedCandidateDto generateMockParsedCandidate(UUID jdId) {
        double score = (jdId != null) ? 85.0 : 0.0;
        return ParsedCandidateDto.builder()
                .fullName("Huynh Tan Loc (Mocked)")
                .email("huynhtanloc.mock@example.com")
                .phoneNumber("+84 999 888 777")
                .skills(List.of("Spring Boot", "React", "RabbitMQ", "Redis", "PostgreSQL", "pgvector"))
                .education("Information Technology Internship Student")
                .experienceYears(1.0)
                .summary("Passionate software engineer intern specializing in Spring Boot backends, RabbitMQ queue buffering, and pgvector semantic searches.")
                .compatibilityScore(score)
                .build();
    }
}
