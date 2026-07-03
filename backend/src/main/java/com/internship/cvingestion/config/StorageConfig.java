package com.internship.cvingestion.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@Slf4j
@Getter
public class StorageConfig {

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        try {
            Path path = Paths.get(uploadDir);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                log.info("Initialized local upload directory at: {}", path.toAbsolutePath());
            }
        } catch (IOException e) {
            log.error("Could not create upload directory: {}", uploadDir, e);
            throw new RuntimeException("Failed to initialize upload directory", e);
        }
    }
}
