package com.internship.cvworker;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CvWorkerApplication {
    public static void main(String[] args) {
        // Load environment variables from .env
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        // Fallback to parent directory if .env not found in current directory
        if (dotenv.get("RABBITMQ_HOST") == null) {
            dotenv = Dotenv.configure()
                    .directory("..")
                    .ignoreIfMissing()
                    .load();
        }

        for (DotenvEntry entry : dotenv.entries()) {
            System.setProperty(entry.getKey(), entry.getValue());
        }
        SpringApplication.run(CvWorkerApplication.class, args);
    }
}
