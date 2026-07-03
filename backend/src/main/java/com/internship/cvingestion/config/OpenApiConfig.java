package com.internship.cvingestion.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cvIngestionOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CV Ingestion Service API")
                        .description("REST API contracts for uploading CVs and querying parsing jobs status.")
                        .version("v1.0.0"));
    }
}
