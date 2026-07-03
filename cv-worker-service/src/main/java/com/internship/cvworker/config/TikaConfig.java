package com.internship.cvworker.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.InputStream;

@Slf4j
@Configuration
public class TikaConfig {

    @Bean
    public Tika tika() {
        // Try to load from external config file (Docker path)
        File externalConfig = new File("/app/tika-config.xml");
        if (externalConfig.exists()) {
            try {
                org.apache.tika.config.TikaConfig config = new org.apache.tika.config.TikaConfig(externalConfig);
                log.info("Loaded Tika configuration from external file: {}", externalConfig.getAbsolutePath());
                return new Tika(config);
            } catch (Exception e) {
                log.warn("Failed to load external tika-config.xml, trying classpath...", e);
            }
        }

        // Try to load from classpath (local dev)
        try (InputStream is = getClass().getResourceAsStream("/tika-config.xml")) {
            if (is != null) {
                org.apache.tika.config.TikaConfig config = new org.apache.tika.config.TikaConfig(is);
                log.info("Loaded Tika configuration from classpath /tika-config.xml");
                return new Tika(config);
            }
        } catch (Exception e) {
            log.warn("Failed to load classpath tika-config.xml, using default Tika config (no OCR)", e);
        }

        log.info("Using default Tika configuration (OCR disabled)");
        return new Tika();
    }
}
