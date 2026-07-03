# Stage 1: Build JAR inside container
FROM maven:3.8-openjdk-17-slim AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Production runtime (Debian-based to support Tesseract OCR)
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Install Tesseract OCR + Vietnamese language pack + Ghostscript (for PDF rendering)
RUN apt-get update && apt-get install -y --no-install-recommends \
    tesseract-ocr \
    tesseract-ocr-eng \
    tesseract-ocr-vie \
    ghostscript \
    libfreetype6 \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Copy application JAR
COPY --from=build /app/target/*.jar app.jar

# Copy Tika OCR configuration
COPY src/main/resources/tika-config.xml /app/tika-config.xml

EXPOSE 8081
CMD ["java", "-Dtika.config=/app/tika-config.xml", "-jar", "app.jar"]
