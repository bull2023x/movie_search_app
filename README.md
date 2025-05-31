# movie_search_app
This repository contains a two‐phase Spring Boot application that provides:  Movie Search API (Phase 1) A RESTful service backed by a MySQL “Sakila” sample database.  OCR‐Enabled “Poster Info” API (Phase 2) An extension on top of Phase 1: users can upload a movie poster image (JPEG, PNG, etc.).



Movie Search & OCR Spring Boot Application
This repository contains a two‐phase Spring Boot application that provides:

Movie Search API (Phase 1)
A RESTful service backed by a MySQL “Sakila” sample database that allows clients to look up movie metadata by title (exact match or case‐insensitive partial search).

OCR‐Enabled “Poster Info” API (Phase 2)
An extension on top of Phase 1: users can upload a movie poster image (JPEG, PNG, etc.), the service runs Tesseract OCR to extract the film’s title text from the image, then automatically invokes the Phase 1 endpoints to fetch and return the movie data.

Below you’ll find:

A concise overview of what the application does

High‐level architecture & technology stack

API endpoints with example payloads

How to run/build locally

How the OCR integration works

Notes on Docker‐based packaging

Feel free to copy this content into your GitHub README (or similar “notes”) so that colleagues can quickly understand the system.

1. Overview
What does this app achieve?

Phase 1 (Movie Search API)

Connects to a MySQL “Sakila” database (schema + sample data).

Exposes two endpoints:

GET /api/film-by-title?title=<exactTitle>
– Returns the single Film record whose title exactly matches <exactTitle>.
– Example: /api/film-by-title?title=ACADEMY%20DINOSAUR

GET /api/film-search?title=<substring>
– Returns a list of all Film records whose title contains <substring> (case‐insensitive).
– Example: /api/film-search?title=terminator might return “TERMINATOR CLUB,” “VELVET TERMINATOR,” etc.

Phase 2 (OCR + Poster Info API)

Adds a new endpoint:
POST /api/poster-info
– Accepts a multipart/form-data file upload (an image of a movie poster).
– Internally, uses Tess4J (a Java JNA wrapper for Tesseract) to run OCR on the uploaded image.
– Parses out the recognized text to guess the movie’s title (simple uppercase normalization + string‐matching heuristics).
– Calls either the exact‐match endpoint or the “contains” search endpoint from Phase 1.
– Returns the matching Film data as JSON (if found), or 404 if no matching title can be extracted.

End result:
From your local machine (or any HTTP client), you can:

Search movies by title substring.

Upload a scanned/poster image to have the service “read” the title text, then automatically fetch metadata for you.

2. High-Level Architecture & Tech Stack
scss
Copy
Edit
┌──────────────────────────────────────────────────────────────────┐
│                            Client                              │
│  (curl, Postman, web page, or ChatGPT “plugin” call via HTTP)  │
└──────────────────────────────────────────────────────────────────┘
                     │                       ▲
                     ▼                       │
┌──────────────────────────────────────────────────────────────────┐
│                       Spring Boot App                           │
│  (Phase 1 + Phase 2 combined)                                   │
│ ┌──────────────────────────────────────────────────────────────┐ │
│ │   ┌─────────────┐  ┌────────────────────────────────────────┐ │ │
│ │   │  REST APIs  │  │   OCR Controller (Tess4J + Tesseract)   │ │ │
│ │   │(FilmController│  │                                        │ │ │
│ │   │ + SearchRepo) │  │  • Accepts poster image (JPEG/PNG/…)    │ │ │
│ │   │             │  │  • Runs Tesseract OCR on image          │ │ │
│ │   │             │  │  • Extracts movie title text            │ │ │
│ │   │             │  │  • Delegates to “FilmController” to      │ │ │
│ │   │             │  │    fetch database record(s).           │ │ │
│ │   └─────────────┘  └────────────────────────────────────────┘ │ │
│ └──────────────────────────────────────────────────────────────┘ │
│                     ▲                                         │
│                     │  (JPA/Hibernate)                         │
│                     ▼                                         │
│             ┌───────────────────────────────────┐              │
│             │   MySQL “Sakila” Database         │              │
│             │   (films, actors, categories, …)   │              │
│             │                                    │              │
│             └───────────────────────────────────┘              │
└──────────────────────────────────────────────────────────────────┘
Language & Framework

Java 17 (“LTS”)

Spring Boot 3.5 (Spring MVC, Spring Data JPA, Spring Web)

Database

MySQL 8.0 (Sakila sample schema + data)

Hibernate ORM 6.x via Spring Data JPA

HikariCP connection pooling

OCR Library

Tess4J 5.x (Java wrapper for Tesseract OCR engine)

Requires Tesseract “tessdata” language packs installed on the host (e.g. /usr/share/tesseract-ocr/5/tessdata)

Packaging & Runtime

Packaged as a fat JAR via spring-boot-maven-plugin (Phase 1 & 2 combined)

Docker multi-stage build to produce a runnable container image

Docker Compose file to spin up & initialize both MySQL (“sakila”) and the Spring Boot API together

Operating System / Environment

Developed & tested on Ubuntu 24.04 LTS (guest VM)

Docker / Docker Compose (using Linux KVM virtualization on Oracle Linux 9 host)

Port 8080 exposed for the API; port 3306 for MySQL (only exposed to host via Compose, can be locked down if needed)

3. API Endpoints & Examples
Phase 1 Endpoints
GET /api/film-by-title?title=<exactTitle>

Description:
Fetch the single film whose title exactly matches <exactTitle> (case‐sensitive by default in MySQL, but you can adjust collation if needed).

Request:

bash
Copy
Edit
GET http://localhost:8080/api/film-by-title?title=ACADEMY%20DINOSAUR
Response (HTTP 200):

json
Copy
Edit
{
  "filmId": 1,
  "title": "ACADEMY DINOSAUR",
  "description": "A Epic Drama of a Feminist And a Mad Scientist who must Battle a Teacher in The Canadian Rockies",
  "releaseYear": "2006-01-01",
  "length": 86,
  "lastUpdate": "2006-02-15T05:03:42"
}
Response (HTTP 404 if not found):

json
Copy
Edit
{ "timestamp": "...", "status": 404, "error": "Not Found", "path": "/api/film-by-title" }
GET /api/film-search?title=<substring>

Description:
Return a list of all films whose titles contain <substring>, ignoring case.

Request:

bash
Copy
Edit
GET http://localhost:8080/api/film-search?title=terminator
Response (HTTP 200):

json
Copy
Edit
[
  {
    "filmId": 884,
    "title": "TERMINATOR CLUB",
    "description": "A Touching Story of a Crocodile And a Girl who must Sink a Man in The Gulf of Mexico",
    "releaseYear": "2006-01-01",
    "length": 88,
    "lastUpdate": "2006-02-15T05:03:42"
  },
  {
    "filmId": 938,
    "title": "VELVET TERMINATOR",
    "description": "A Lacklusture Tale of a Pastry Chef And a Technical Writer who must Confront a Crocodile in An Abandoned Amusement Park",
    "releaseYear": "2006-01-01",
    "length": 173,
    "lastUpdate": "2006-02-15T05:03:42"
  }
]
Phase 2 Endpoint
POST /api/poster-info

Description:
Accepts a movie‐poster image, runs OCR to detect the title, then returns the matching film data (using one of the Phase 1 methods).

Request:

bash
Copy
Edit
curl -X POST \
  -F "file=@/path/to/some-movie-poster.jpg" \
  http://localhost:8080/api/poster-info
Behavior:

Spring MVC’s @RequestPart MultipartFile file receives the image.

Tess4J initializes a Tesseract instance with language = "eng" and tessdata path from application.properties.

OcrController calls tess.doOCR(uploadedImageFile) → raw text.

Some simple post-processing (e.g. split on newlines, pick longest uppercase word, trim non-alphabetic chars) to isolate a likely title.

If an exact‐match film is found (findByTitle(...)), return that. Otherwise, try the “containsIgnoreCase” repository method to return a list.

If still no match, return HTTP 404 “Not Found.”

Response (HTTP 200 – exact match):

json
Copy
Edit
{
  "filmId": 123,
  "title": "CASABLANCA SUPER",
  "description": "A Amazing Panorama of a Crocodile And a Forensic Psychologist who must Pursue a Secret Agent in The First Manned Space Station",
  "releaseYear": "2006-01-01",
  "length": 85,
  "lastUpdate": "2006-02-15T05:03:42"
}
Response (HTTP 200 – partial match):

json
Copy
Edit
[
  { /* Film record matching “CASAB” substring */ },
  { /* Another Film record if multiple “CASAB” hits */ }
]
Response (HTTP 404 if OCR fails or no matching film):

json
Copy
Edit
{ "timestamp": "...", "status": 404, "error": "Not Found", "path": "/api/poster-info" }
4. How to Run & Build Locally (Ubuntu 24.04)
Below is a step-by-step guide (with exact commands) to set up the entire system on a fresh Ubuntu 24.04 machine. It assumes you have:

Ubuntu 24.04 LTS (guest VM under KVM or similar)

Java 17 JDK

Maven 3.x

MySQL client (for manual verification)

Tess4J + Tesseract OCR engine

Docker & Docker Compose (Phase 1 can run standalone without Docker, but Phase 2’s Docker Compose is included)

Prerequisites
Update & install essential tools

bash
Copy
Edit
sudo apt update && sudo apt upgrade -y
sudo apt install -y git curl unzip build-essential
Install Java 17 (OpenJDK)

bash
Copy
Edit
sudo apt install -y openjdk-17-jdk
java -version
# Expect: openjdk version "17.x..."
Install Maven 3

bash
Copy
Edit
sudo apt install -y maven
mvn -version
# Expect: Apache Maven 3.x.x
Install MySQL client (for manual DB checks)

bash
Copy
Edit
sudo apt install -y mysql-client
Install Tesseract OCR + language data

bash
Copy
Edit
sudo apt install -y tesseract-ocr libtesseract-dev libleptonica-dev
sudo apt install -y tesseract-ocr-eng  # English language pack (adds /usr/share/tesseract-ocr/5/tessdata/eng.traineddata)
# Verify:
tesseract --version
ls /usr/share/tesseract-ocr/5/tessdata
# Expect to see: eng.traineddata, etc.
Install Tess4J dependencies
Tess4J is pulled in automatically via Maven in the project’s pom.xml. You only need the local Tesseract binaries installed above.

Phase 1: Movie Search API
4.1.1. Clone the repository (if not already)
If your code is not yet in GitHub, copy the entire project folder onto the Ubuntu VM (e.g. via scp, or unzip from a .tar.gz). Then:

bash
Copy
Edit
cd ~
git clone https://github.com/your‐username/sakila-movie-search.git
cd sakila-movie-search
(If you’re starting fresh, create an empty folder and copy in the files: pom.xml, src/, Dockerfile, docker-compose.yml, sakila-db/.)

4.1.2. Prepare the MySQL “Sakila” database locally (Phase 1 only)
Download / unzip the “sakila” schema & data
You should have two SQL files in sakila-db/:

sakila-schema.sql

sakila-data.sql

If not, grab them from the official MySQL repository or our sakila-db.zip:

bash
Copy
Edit
unzip sakila-db.zip -d sakila-db
ls sakila-db
# Expect: sakila-schema.sql  sakila-data.sql
Log in to your local MySQL server
(Assumes you already have a running MySQL 8.0 server on Ubuntu.)

bash
Copy
Edit
sudo systemctl start mysql
mysql -u root -p
Enter your root password when prompted.

Create a “sakila” database and load schema & data

sql
Copy
Edit
CREATE DATABASE sakila CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
EXIT;
mysql -u root -p sakila < sakila-db/sakila-schema.sql
mysql -u root -p sakila < sakila-db/sakila-data.sql
Create (optional) a dedicated user for the app
You can either let the Spring Boot app connect as root (insecure) or create a dedicated user sakila_app:

sql
Copy
Edit
mysql -u root -p
CREATE USER 'sakila_app'@'localhost' IDENTIFIED BY 'sakila_pass';
GRANT SELECT ON sakila.* TO 'sakila_app'@'localhost';
FLUSH PRIVILEGES;
EXIT;
Configure src/main/resources/application.properties
Open application.properties and set:

properties
Copy
Edit
spring.datasource.url=jdbc:mysql://localhost:3306/sakila
spring.datasource.username=sakila_app      # or "root" if you prefer
spring.datasource.password=sakila_pass     # or your root password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate settings:
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
# (You can remove explicit dialect, as Hibernate will pick up MySQL automatically.)
4.1.3. Build & Run Phase 1 (without Docker)
Compile & package as a “fat JAR”

bash
Copy
Edit
mvn clean package
You should see:

pgsql
Copy
Edit
[INFO] Building sakila-api 0.0.1-SNAPSHOT
…
[INFO] Building jar: /home/…/target/sakila-api-0.0.1-SNAPSHOT.jar
Run the application

bash
Copy
Edit
java -jar target/sakila-api-0.0.1-SNAPSHOT.jar
By default, Spring Boot listens on port 8080.

Check logs for “Tomcat started on port 8080”.

Verify Phase 1 endpoints

Exact‐match:

bash
Copy
Edit
curl "http://localhost:8080/api/film-by-title?title=ACADEMY%20DINOSAUR"
Substring search:

bash
Copy
Edit
curl "http://localhost:8080/api/film-search?title=terminator"
You should get JSON responses as described above.

If port 8080 is in use

Sometimes another process (or a previously launched instance) is still listening on 8080.

Find and kill it:

bash
Copy
Edit
sudo lsof -t -i :8080
sudo kill -9 <PID>
Then re-run java -jar … again.

At this point, Phase 1 is complete: you have a local Spring Boot API serving movie‐search results from a local MySQL “Sakila” database.

Phase 2: OCR + Poster Info
4.2.1. Add Tess4J & OCR Controller
Modify pom.xml
Ensure you have these added to <dependencies>:

xml
Copy
Edit
<!-- Phase 1 dependencies -->
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

<!-- Phase 2: Tess4J OCR -->
<dependency>
  <groupId>net.sourceforge.tess4j</groupId>
  <artifactId>tess4j</artifactId>
  <version>5.6.0</version>
</dependency>
Note: Version 5.6.0 (or latest available). Avoid duplicating/deprecating multiple Tess4J versions.

Add OCR configuration to src/main/resources/application.properties (near the bottom):

properties
Copy
Edit
# Tess4J/Tesseract datapath: point to your Tesseract “tessdata” folder
tess4j.datapath=/usr/share/tesseract-ocr/5/tessdata
Adjust if your OS has Tesseract installed elsewhere (e.g. /usr/share/tesseract-ocr/4.00/tessdata/).

Create OcrController.java under:

swift
Copy
Edit
src/main/java/com/example/sakila_api/controller/OcrController.java
(If your main package is com.example.sakila_api, keep consistency under /controller.)

java
Copy
Edit
package com.example.sakila_api.controller;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

@RestController
public class OcrController {
  // Inject via application.properties:
  @Value("${tess4j.datapath}")
  private String tessDataPath;

  private final FilmController filmController; // reuse existing FilmController

  public OcrController(FilmController filmController) {
    this.filmController = filmController;
  }

  @PostMapping("/api/poster-info")
  public ResponseEntity<?> extractPosterInfo(@RequestPart("file") MultipartFile file) {
    if (file.isEmpty()) {
      return ResponseEntity.badRequest().body("No file uploaded");
    }

    // Save uploaded file to a temporary local file:
    File tempImageFile;
    try {
      String originalFilename = StringUtils.getFilename(file.getOriginalFilename());
      tempImageFile = File.createTempFile("ocr_input_", "_" + originalFilename);
      file.transferTo(tempImageFile);
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                           .body("Failed to save uploaded image");
    }

    // Initialize Tesseract instance:
    Tesseract tesseract = new Tesseract();
    tesseract.setDatapath(tessDataPath);
    tesseract.setLanguage("eng");

    String ocrResult;
    try {
      ocrResult = tesseract.doOCR(tempImageFile);
    } catch (TesseractException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                           .body("OCR processing failed: " + e.getMessage());
    } finally {
      // Delete temp file on exit (optional):
      tempImageFile.delete();
    }

    // Basic post-processing: extract likely title string (first non-empty, uppercase line)
    String[] lines = ocrResult.split("\\R"); // split on any newline
    String foundTitle = null;
    for (String line : lines) {
      String trimmed = line.trim();
      if (trimmed.length() >= 3 && trimmed.equals(trimmed.toUpperCase())) {
        // pick first ALL‐CAPS line of length >= 3
        foundTitle = trimmed;
        break;
      }
    }
    if (foundTitle == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
                           .body("No movie title detected via OCR");
    }

    // 1) Try exact match
    ResponseEntity<?> exactMatch = filmController.getByTitle(foundTitle);
    if (exactMatch.getStatusCode().is2xxSuccessful()) {
      return exactMatch;
    }

    // 2) Try “containsIgnoreCase” fallback
    List<?> partialMatchList = filmController.searchByTitleContains(foundTitle);
    if (!partialMatchList.isEmpty()) {
      return ResponseEntity.ok(partialMatchList);
    }

    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                         .body("No matching film found for \"" + foundTitle + "\"");
  }
}
Notes on the code above:

We re‐use the existing FilmController (injected) to perform database lookups.

We assume FilmController has an additional method searchByTitleContains(...) that wraps findByTitleContainingIgnoreCase(...). If not, add this to FilmController.java:

java
Copy
Edit
// In FilmController.java
@GetMapping("/api/film-search")
public ResponseEntity<List<Film>> searchByTitleContains(@RequestParam String title) {
  List<Film> results = repo.findByTitleContainingIgnoreCase(title);
  return ResponseEntity.ok(results);
}
Tesseract requires a local “tessdata” folder. We set tess4j.datapath=/usr/share/tesseract-ocr/5/tessdata so that tesseract.setDatapath() can find the .traineddata files.

The heuristic to pick an ALL-CAPS line of length ≥ 3 is simplistic; for production you may refine (strip punctuation, handle multi‐line titles, etc.).

4.2.2. Verify Phase 2 Locally (without Docker)
Rebuild & repackage (Phase 2 changes included)

bash
Copy
Edit
mvn clean package
Run the app (assuming MySQL “sakila” already set up)

bash
Copy
Edit
java -jar target/sakila-api-0.0.1-SNAPSHOT.jar
Test OCR upload
Place a sample poster on your filesystem, for instance ~/movie_poster.jpg (must be a true JPEG/PNG etc. containing legible English text).

bash
Copy
Edit
curl -X POST \
  -F "file=@/home/ubuntu/movie_poster.jpg" \
  http://localhost:8080/api/poster-info
If OCR successfully reads “CASABLANCA” (all-caps on the poster image), the service returns JSON for the “CASABLANCA” film from Sakila. If spelled differently or Tesseract fails, you’ll get 404.

5. Docker & Docker Compose Packaging
Once both phases work locally, you can containerize everything so that colleagues can run a single docker-compose up -d to stand up:

MySQL service (initializes the “sakila” schema & data automatically, via docker-entrypoint-initdb.d/)

Spring Boot API service (Phase 1 + 2 combined, pre‐built JAR inside container)

Below is a sample Dockerfile and docker-compose.yml—feel free to adapt paths/usernames as needed.

5.1. Dockerfile (project root)
dockerfile
Copy
Edit
# ── STAGE 1: Build the Spring Boot JAR using Maven ──
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# Copy Maven wrapper + pom.xml first, so Docker can leverage caching
COPY pom.xml mvnw mvnw.cmd ./
COPY .mvn/ .mvn/

# Copy the entire source tree
COPY src/ src/

# Ensure Maven wrapper is executable
RUN chmod +x ./mvnw

# Build the “fat JAR”
RUN ./mvnw clean package -DskipTests

# ── STAGE 2: Package into a lean runtime image ──
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy only the built JAR from the builder stage
COPY --from=build /app/target/sakila-api-0.0.1-SNAPSHOT.jar app.jar

# Expose HTTP port
EXPOSE 8080

# Run the JAR by default
ENTRYPOINT ["java","-jar","/app/app.jar"]
Note:

We use the official eclipse-temurin:17-jdk-jammy image in Stage 1 to compile with Maven.

Then we switch to a slim eclipse-temurin:17-jre-jammy base for Stage 2 (runtime).

The final image contains only the JAR and the JRE, keeping the size small.

5.2. docker-compose.yml (project root)
yaml
Copy
Edit
version: "3.8"  # (Docker Compose v2+ will ignore this field, but it’s safe to declare)

services:
  db:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root_password_here      # change as needed
      MYSQL_DATABASE: sakila
      MYSQL_USER:    sakila_app
      MYSQL_PASSWORD:sakila_pass
    ports:
      - "3306:3306"   # Expose MySQL to host on port 3306 (optional; remove if not needed)
    volumes:
      - ./sakila-db/sakila-schema.sql:/docker-entrypoint-initdb.d/sakila-schema.sql
      - ./sakila-db/sakila-data.sql:/docker-entrypoint-initdb.d/sakila-data.sql

  api:
    image: yourID/sakila-api:latest   # after you `docker build` and `docker tag yourID/sakila-api:latest .`
    depends_on:
      - db
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL:      jdbc:mysql://db:3306/sakila
      SPRING_DATASOURCE_USERNAME: sakila_app
      SPRING_DATASOURCE_PASSWORD: sakila_pass
      # If you want to override tessdata path inside container (unlikely), add:
      # TESS4J_DATAPATH: /usr/share/tesseract-ocr/5/tessdata
How it works

When you run docker-compose up -d, Compose will:

Pull & start mysql:8.0. Because we mount sakila-schema.sql and sakila-data.sql into /docker-entrypoint-initdb.d/, the official MySQL entrypoint automatically creates the sakila database, runs schema + data scripts, and sets up user sakila_app with password sakila_pass.

Build (or pull) your Spring Boot yourID/sakila-api:latest image, then start it. Since api depends_on db, it will wait for MySQL to begin.

The API sees SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/sakila (the hostname db resolves to the MySQL container). It connects and runs normally.

Port mapping:

Host 3306 → container 3306 (MySQL). If you don’t want MySQL on your host, remove the ports line.

Host 8080 → container 8080 (Spring Boot). You can then point your browser or curl at http://localhost:8080/api/....

6. How to Share / Distribute
Important: Your company policy prohibits using Docker Hub, but you have alternatives:

Share the source repository

Push all code (including Dockerfile, docker-compose.yml, sakila-db/, and Maven sources) to a GitHub (or GitLab) repository.

Colleagues git clone and run docker-compose up -d to build and launch from source.

Pros: everything is version‐controlled, no large binary blobs in Git.

Cons: each dev must have Docker + Maven locally to build the image from Dockerfile.

Distribute the pre‐built image as a .tar file

On your Ubuntu VM (or a build server), run:

bash
Copy
Edit
docker build -t yourID/sakila-api:latest .
docker save -o sakila-api-image.tar yourID/sakila-api:latest
Now you have sakila-api-image.tar (often ~200–300 MB).

Optionally compress it:

bash
Copy
Edit
gzip sakila-api-image.tar   # → sakila-api-image.tar.gz
Transfer that .tar.gz (or .tar) to colleagues via corporate file-share (SCP, shared network drive, OneDrive, etc.).

Colleagues do:

bash
Copy
Edit
docker load -i sakila-api-image.tar.gz
docker tag yourID/sakila-api:latest yourID/sakila-api:latest
Then they run docker-compose up -d (with the same docker-compose.yml), which will find/use yourID/sakila-api:latest locally (no rebuild needed).

Use GitHub Container Registry (GHCR)

If company policy permits, you can push the image to GitHub’s own registry:

bash
Copy
Edit
# 1) Log in to GHCR:
echo $GITHUB_TOKEN | docker login ghcr.io -u YOUR_GITHUB_USERNAME --password-stdin
# 2) Tag & push:
docker tag sakila-api:latest ghcr.io/YOUR_GITHUB_USERNAME/sakila-api:latest
docker push ghcr.io/YOUR_GITHUB_USERNAME/sakila-api:latest
Colleagues “docker pull ghcr.io/YourUsername/sakila-api:latest”

Then run with your docker-compose.yml (update the api.image: line to ghcr.io/YourUsername/sakila-api:latest).

Pros: image distribution is handled by GHCR; versioning & access control are integrated with GitHub.

Cons: GHCR usage may be restricted by corporate policy or require special PAT scopes.

Recommendation: Since you said Docker Hub is disallowed, choose either “Share Source + Rebuild Locally” (option 1) or “Distribute a .tar Snapshot” (option 2). Both work well in an air-gapped or corporate-policy environment.

7. Sample README Snippet for GitHub
Below is a condensed “README.md” style summary you can place at your repo’s root. Feel free to adapt wording/titles as needed:

markdown
Copy
Edit
# Sakila Movie Search & OCR API

A two‐phase Spring Boot application that:

1. **Phase 1: Movie Search API**  
   • Provides REST endpoints to query a MySQL “Sakila” sample database by film title (exact and substring).  

2. **Phase 2: OCR “Poster Info” API**  
   • Accepts an uploaded movie poster image, uses Tesseract OCR (via Tess4J) to extract the film’s title,  
   • Then returns the matching film metadata via Phase 1 endpoints.

## Technologies Used

- Java 17, Spring Boot 3.x (Spring Web, Spring Data JPA)  
- MySQL 8.0 (Sakila sample schema + data)  
- Hibernate 6.x (JPA) & HikariCP  
- Tess4J 5.x + Tesseract OCR  
- Docker & Docker Compose for containerization  
- Ubuntu 24.04 LTS (Dev environment), Oracle Linux 9 (KVM host)

## Quickstart (Local, No Docker)

1. **Install prerequisites:**  
   ```bash
   sudo apt update && sudo apt install -y openjdk-17-jdk maven mysql-client tesseract-ocr tesseract-ocr-eng
Prep MySQL “sakila” DB:

bash
Copy
Edit
mysql -u root -p
CREATE DATABASE sakila CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
EXIT
mysql -u root -p sakila < sakila-db/sakila-schema.sql
mysql -u root -p sakila < sakila-db/sakila-data.sql
Create dedicated app user (optional):

sql
Copy
Edit
mysql -u root -p
CREATE USER 'sakila_app'@'localhost' IDENTIFIED BY 'sakila_pass';
GRANT SELECT ON sakila.* TO 'sakila_app'@'localhost';
FLUSH PRIVILEGES; EXIT;
Configure application.properties:

properties
Copy
Edit
# Phase 1 DB config
spring.datasource.url=jdbc:mysql://localhost:3306/sakila
spring.datasource.username=sakila_app
spring.datasource.password=sakila_pass

# Phase 2 OCR config
tess4j.datapath=/usr/share/tesseract-ocr/5/tessdata

spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
Build & run:

bash
Copy
Edit
mvn clean package
java -jar target/sakila-api-0.0.1-SNAPSHOT.jar
Test Phase 1:

perl
Copy
Edit
curl "http://localhost:8080/api/film-by-title?title=ACADEMY%20DINOSAUR"
curl "http://localhost:8080/api/film-search?title=terminator"
Test Phase 2:

bash
Copy
Edit
curl -X POST -F "file=@/path/to/movie-poster.jpg" http://localhost:8080/api/poster-info
Quickstart (Docker Compose)
Build the Docker image (once):

bash
Copy
Edit
docker build -t yourID/sakila-api:latest .
Spin up DB + API:

bash
Copy
Edit
docker-compose up -d
Verify

MySQL is listening on container “db” (mapped to host 3306).

Spring API on http://localhost:8080

bash
Copy
Edit
curl "http://localhost:8080/api/film-by-title?title=TERMINATOR"
curl -X POST -F "file=@/path/to/movie-poster.jpg" http://localhost:8080/api/poster-info
How to Share This Container
Option 1: Share “Source + Compose” (No Docker Hub)

Push this entire repo (source code, Dockerfile, docker-compose.yml, sakila-db/) into a GitHub (or other) repository.

Colleagues git clone and run docker-compose up -d. Docker Compose will build the API image locally using the Dockerfile and initialize MySQL with the Sakila SQL scripts.

Option 2: Distribute Prebuilt Image as .tar

On your build machine:

bash
Copy
Edit
docker build -t yourID/sakila-api:latest .
docker save -o sakila-api-image.tar yourID/sakila-api:latest
gzip sakila-api-image.tar   # → sakila-api-image.tar.gz
Transfer sakila-api-image.tar.gz + docker-compose.yml + sakila-db/ folder to your colleague (via secure file share, SFTP, etc.).

Colleague does:

bash
Copy
Edit
docker load -i sakila-api-image.tar.gz
docker-compose up -d
They can now curl http://localhost:8080/api/... exactly as above.

Option 3: GitHub Container Registry (GHCR)

Tag & push your image:

bash
Copy
Edit
echo $GITHUB_TOKEN | docker login ghcr.io -u YOUR_GITHUB_USERNAME --password-stdin
docker tag yourID/sakila-api:latest ghcr.io/YOUR_GITHUB_USERNAME/sakila-api:latest
docker push ghcr.io/YOUR_GITHUB_USERNAME/sakila-api:latest
Colleagues pull:

bash
Copy
Edit
echo $GH_TOKEN | docker login ghcr.io -u THEIR_GITHUB_USERNAME --password-stdin
docker pull ghcr.io/YOUR_GITHUB_USERNAME/sakila-api:latest
docker run -p 8080:8080 ghcr.io/YOUR_GITHUB_USERNAME/sakila-api:latest
Or include GHCR path in docker-compose.yml directly.

8. At Completion of Each Phase
Phase 1 Complete
– You have a standalone Spring Boot JAR (sakila-api-0.0.1-SNAPSHOT.jar) that:

Connects to a local MySQL “Sakila” schema.

Exposes /api/film-by-title and /api/film-search for exact/partial title lookups.

Phase 2 Complete
– You have extended the same Spring Boot App with:

An OCR controller (/api/poster-info) that:

Accepts a poster image upload

Runs Tesseract OCR (via Tess4J) to detect the movie title text

Delegates to your Phase 1 repository/controller to retrieve film metadata

Returns the film metadata JSON automatically.
– You have a working Docker image (yourID/sakila-api:latest) that can be shared or deployed anywhere (local, corporate Docker registry, GitHub Container Registry).

“Explanation of this App” (To Upload as a Note on GitHub)
Use the above “# Movie Search & OCR Spring Boot Application” document as your main README. In your repo’s root, simply create README.md with that content. It covers:

What the app does (Phase 1 & Phase 2)

Technology stack

API endpoints & JSON examples

Step-by-step local setup (Ubuntu 24.04) without Docker

Step-by-step with Docker & Docker Compose

How to distribute/share the container image

Sharing that README.md in GitHub makes it easy for colleagues to clone, run, or load the container.

