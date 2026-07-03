package com.internship.cvingestion;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CvIngestionApplication {
    public static void main(String[] args) {
        // Load environment variables from .env
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        for (DotenvEntry entry : dotenv.entries()) {
            System.setProperty(entry.getKey(), entry.getValue());
        }
        SpringApplication.run(CvIngestionApplication.class, args);
    }
}
