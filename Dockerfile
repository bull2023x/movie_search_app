# ---------- Build stage ----------
FROM maven:3.8-openjdk-17 AS builder
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# ---------- Runtime stage ----------
FROM openjdk:17-jdk-slim
WORKDIR /app

# === Install Tesseract OCR + English + Japanese data ===
#    (Japanese optional but useful for multilingual posters)
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        tesseract-ocr \
        tesseract-ocr-eng \
        tesseract-ocr-jpn && \
    rm -rf /var/lib/apt/lists/*

# === Environment variable for Tess4J / OCR ===
ENV TESSDATA_PREFIX=/usr/share/tesseract-ocr/4.00/tessdata

# Copy built JAR from builder stage
COPY --from=builder /workspace/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
