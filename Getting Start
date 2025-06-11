
markdown
Copy
## Getting Started

These instructions will help you get the Movie Search + OCR app up and running quickly.

### Prerequisites

- **Git**  
- **Java 17+** (or Dockerized build below)  
- **Maven 3.8+** (only for a native build)  
- **Docker & Docker Compose** (for containerized deployment)

---

### 1. Clone the repo

```bash
git clone https://github.com/bull2023x/movie_search_app.git
cd movie_search_app
2A. Native build & run
If you prefer to run directly on your machine:

bash
Copy
# Build the Spring-Boot JAR
mvn clean package -DskipTests

# Run the app
java -jar target/sakila-api-0.0.1-SNAPSHOT.jar
Then browse to http://localhost:8080 and upload a poster.

2B. Docker Compose
If you have Docker & Compose installed, this is the easiest:

bash
Copy
# Stop any running stack
docker-compose down

# Build fresh images (including OCR + DB)
docker-compose build --no-cache

# Start the containers
docker-compose up
Visit http://localhost:8080 in your browser.

3. One-click demo scripts
run-demo.sh (Linux / macOS)
bash
Copy
#!/usr/bin/env bash
set -e
git pull
docker-compose down
mvn clean package -DskipTests
docker-compose build --no-cache
docker-compose up
Make it executable:

bash
Copy
chmod +x run-demo.sh
run-demo.ps1 (Windows PowerShell)
powershell
Copy
git pull
docker-compose down
mvnw.cmd clean package -DskipTests
docker-compose build --no-cache
docker-compose up
4. Pinning a Release
Go to the Releases tab in this repo.

Click Draft a new release.

Tag your current main commit (e.g. v1.0.0) and give it a title/description.

Publish—now users can clone a stable snapshot instead of the bleeding edge.

