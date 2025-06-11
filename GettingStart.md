Movie Search + OCR Demo: Getting Started

This guide walks you through cloning, building, and running the Movie Search + OCR application—either natively or with Docker.

Prerequisites

・Git

・Java 17+ (needed for native build) or Dockerized build below

・Maven 3.8+ (or use the included Maven Wrapper mvnw)

・Docker & Docker Compose (for containerized deployment)

1. Clone the Repository

```
git clone https://github.com/bull2023x/movie_search_app.git
cd movie_search_app
```

2A. Native Build & Run (Local JVM)

# Build the Spring Boot JAR
mvn clean package -DskipTests

# Run the application
java -jar target/sakila-api-0.0.1-SNAPSHOT.jar

Open your browser at http://localhost:8080 and upload a poster to test the OCR + movie lookup.

2B. Docker Compose (Recommended)

# Stop any existing stack
docker-compose down

# Build fresh images (includes Tesseract OCR and MySQL Sakila DB)
docker-compose build --no-cache

# Start the containers
docker-compose up

Browse to http://localhost:8080 to use the upload form. The API is exposed on /api/poster-info and /api/film-search.

3. One‑Click Demo Scripts

run-demo.sh (Linux / macOS)

#!/usr/bin/env bash
set -e

git pull
mvn clean package -DskipTests
docker-compose down
docker-compose build --no-cache
docker-compose up

Make it executable:

chmod +x run-demo.sh

run-demo.ps1 (Windows PowerShell)

git pull
./mvnw.cmd clean package -DskipTests
docker-compose down
docker-compose build --no-cache
docker-compose up

4. Pinning a Release on GitHub

In this repo, go to the Releases tab.

Click Draft a new release.

Tag the current commit (e.g. v1.0.0), add release notes, and Publish.

Users can now download source code for that version directly.

Enjoy your OCR‑powered, containerized Java demo! Feel free to open an issue or PR for enhancements.

