Movie Search Project Overview

This Movie Search Project is divided into two main phases. By completing these phases sequentially, you will:

Build a Spring Boot REST API that searches the Sakila MySQL database for movies (Phase 1)

Extend the service to accept image uploads, perform OCR on movie posters, and return movie details (Phase 2)

Phase Definitions

Phase 1: Create a Spring Boot application that exposes two endpoints against the Sakila database:

Exact match: GET /api/film-by-title?title=<TITLE>

Partial match: GET /api/film-search?title=<TITLE>

Phase 2: Add image upload and OCR using Tess4J/Tesseract:

OCR endpoint: POST /api/ocr returns raw text

Poster‑info endpoint: POST /api/poster-info extracts title via OCR, calls Phase 1 API, returns movie JSON

Technical Requirements

OS: Ubuntu 24.04 LTS on Linux KVM

Java: OpenJDK 17+ (we use 21.0.7)

Spring Boot: 3.5.0

Build Tool: Maven 3.8+

Embedded Server: Tomcat 10.1.41

Database: MySQL 8.x with Sakila schema & data

OCR: Tess4J 5.x & Tesseract 5.x

Utilities: curl, unzip, lsof, kill

What You’ve Achieved

After Phase 1: You have a Spring Boot REST API exposing endpoints to search exact and partial movie titles in the Sakila database.

After Phase 2: Your service now also accepts movie-poster image uploads, extracts the title via OCR (Tess4J/Tesseract), then calls your Phase 1 endpoints to return full movie details.

Phase 1: Implementation Guide (Ubuntu)

0. Generate Spring Boot Skeleton via Spring Initializr

# From your home directory or workspace
dir=~/movie-search
mkdir -p "$dir" && cd "$dir"

# Generate with curl (web UI alternative: https://start.spring.io)
curl https://start.spring.io/starter.zip \
  -d dependencies=web,data-jpa \
  -d type=maven-project \
  -d language=java \
  -d javaVersion=17 \
  -d groupId=com.example \
  -d artifactId=sakila-api \
  -d name=sakila-api \
  -o sakila-api.zip

# Unzip and enter project
unzip sakila-api.zip && cd sakila-api

1. Prepare the Sakila Database

# Unzip and import schema/data
unzip ~/sakila-db.zip -d ~/sakila-db
mysql -u root -p < ~/sakila-db/sakila-schema.sql
mysql -u root -p sakila < ~/sakila-db/sakila-data.sql

2. Configure pom.xml

Ensure these dependencies are present:

<dependencies>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
  </dependency>
  <dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
  </dependency>
</dependencies>

3. Set up Application Properties

Create or edit src/main/resources/application.properties:

spring.datasource.url=jdbc:mysql://localhost:3306/sakila
spring.datasource.username=<DB_USER>
spring.datasource.password=<DB_PASS>
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true

4. Implement Entity, Repository & Controller

Entity: src/main/java/com/example/sakila_api/entity/Film.java

Repository: src/main/java/com/example/sakila_api/repository/FilmRepository.java

Controller: src/main/java/com/example/sakila_api/controller/FilmController.java

Example endpoints:

@GetMapping("/api/film-by-title")
public ResponseEntity<Film> getByTitle(@RequestParam String title) { ... }

@GetMapping("/api/film-search")
public List<Film> searchByTitle(@RequestParam String title) { ... }

5. Build & Run

./mvnw clean package
./mvnw spring-boot:run

6. Verify Endpoints

curl "http://localhost:8080/api/film-by-title?title=ACADEMY%20DINOSAUR"
curl "http://localhost:8080/api/film-search?title=terminator"

Note: Resolving port conflicts
If port 8080 is in use:

sudo lsof -t -i :8080
sudo kill -9 <PID>

Phase 2: Implementation Guide (Ubuntu)

1. Add Tess4J Dependency

In pom.xml:

<dependency>
  <groupId>net.sourceforge.tess4j</groupId>
  <artifactId>tess4j</artifactId>
  <version>5.6.0</version>
</dependency>

2. Configure Tesseract Data Path

Edit src/main/resources/application.properties:

tess4j.datapath=/usr/share/tesseract-ocr/5/tessdata

3. Implement OCR Controller

Create src/main/java/com/example/sakila_api/controller/OcrController.java:

package com.example.sakila_api.controller;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class OcrController {
  @Value("${tess4j.datapath}")
  private String tessDataPath;

  private final FilmController filmController;

  public OcrController(FilmController filmController) {
    this.filmController = filmController;
  }

  @PostMapping("/api/ocr")
  public ResponseEntity<String> ocr(@RequestParam MultipartFile file) throws TesseractException, IOException {
    Tesseract tesseract = new Tesseract();
    tesseract.setDatapath(tessDataPath);
    String text = tesseract.doOCR(file.getInputStream());
    return ResponseEntity.ok(text);
  }

  @PostMapping("/api/poster-info")
  public ResponseEntity<?> posterInfo(@RequestParam MultipartFile file) throws Exception {
    Tesseract tesseract = new Tesseract();
    tesseract.setDatapath(tessDataPath);
    String title = tesseract.doOCR(file.getInputStream()).trim();
    return filmController.getByTitle(title);
  }
}

4. Build & Run

./mvnw clean package
./mvnw spring-boot:run

5. Verify OCR & Poster‑info

curl -X POST -F "file=@/home/<your_user>/movie_poster.jpg" http://localhost:8080/api/ocr
curl -X POST -F "file=@/home/<your_user>/movie_poster.jpg" http://localhost:8080/api/poster-info

Note: Resolving port conflicts

sudo lsof -t -i :8080
sudo kill -9 <PID>

Supplement: Self‑Hosted Plugin for ChatGPT (Advanced)

Generate an OpenAPI spec from your controllers

Create ai-plugin.json and openapi.json manifests at your web root

Expose your service on a reachable host (e.g. 172.16.90.107:8080)

In ChatGPT plugin settings, register your manifest URL

Upload poster → ChatGPT calls /api/poster-info → returns movie info in chat

End of Phase 1 & Phase 2 Guides

