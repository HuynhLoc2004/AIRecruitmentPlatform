package com.internship.cvworker.service.impl;

import com.internship.cvworker.service.TextExtractionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
@Slf4j
public class TikaTextExtractionServiceImpl implements TextExtractionService {

    private final Tika tika;

    @Value("${app.extraction.timeout-ms:30000}")
    private long extractionTimeoutMs;

    @Override
    public String extractText(Path filePath) {
        log.info("Starting text extraction with Apache Tika for: {}. Timeout: {}ms",
                filePath.toAbsolutePath(), extractionTimeoutMs);
        long startTime = System.currentTimeMillis();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            File file = filePath.toFile();
            if (!file.exists()) {
                throw new RuntimeException("Target file not found for extraction: " + filePath.toAbsolutePath());
            }

            Future<String> extractionTask = executor.submit(() -> tika.parseToString(file));
            String extractedText = extractionTask.get(extractionTimeoutMs, TimeUnit.MILLISECONDS);
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("Successfully extracted {} characters from resume in {}ms", 
                    extractedText.length(), duration);

            return extractedText;
        } catch (TimeoutException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Apache Tika text extraction timed out after {}ms for file: {}",
                    duration, filePath, e);
            throw new RuntimeException("Tika text extraction timed out after "
                    + extractionTimeoutMs + "ms", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Apache Tika text extraction was interrupted for file: {}", filePath, e);
            throw new RuntimeException("Tika text extraction interrupted", e);
        } catch (ExecutionException e) {
            log.error("Apache Tika failed to extract text from file: {}", filePath, e.getCause());
            throw new RuntimeException("Tika text extraction failed", e.getCause());
        } catch (Exception e) {
            log.error("Apache Tika failed to extract text from file: {}", filePath, e);
            throw new RuntimeException("Tika text extraction failed", e);
        } finally {
            executor.shutdownNow();
        }
    }
}
