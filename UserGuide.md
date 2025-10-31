# 🎬 Movie Search App — User Manual (ZIP version)

### 🧰 Overview
This Java-based web application demonstrates how to combine:
- **Spring Boot (Java 17)**
- **MySQL Sakila sample database**
- **Tesseract OCR (baked into Docker)**
- **AI-like poster recognition module (Tess4J)**

You can run the entire system **locally** using **Docker Compose** — no need to install Java, MySQL, or Tesseract manually.

---

## 🚀 Quick Start Guide

### 1️⃣ Prerequisites
Before starting, make sure you have **Docker** running:

| OS | Required software |
|----|--------------------|
| macOS | [Docker Desktop for Mac](https://docs.docker.com/desktop/install/mac-install/) |
| Windows | [Docker Desktop for Windows](https://docs.docker.com/desktop/install/windows-install/) |
| Linux | Docker Engine + Docker Compose plugin |

> 💡 You can verify it’s working:
> ```bash
> docker version
> docker compose version
> ```

---

### 2️⃣ Download the App
1. Visit 👉 [https://github.com/bull2023x/movie_search_app](https://github.com/bull2023x/movie_search_app)  
2. Click **Code → Download ZIP**  
3. Extract it, for example to:
   ```
   ~/movie_search_app-main
   ```

---

### 3️⃣ Start the System
Open a terminal (or PowerShell) in that folder and run:

```bash
cd ~/movie_search_app-main
docker compose up --build
```

What happens:
- Docker builds two containers:
  - **movie_search_db** — MySQL 8 with Sakila sample data  
  - **movie_search_api** — Spring Boot App + Tesseract OCR (Eng + Jpn)
- Both containers start automatically.  
- You’ll see logs like:
  ```
  movie_search_db | ready for connections.
  movie_search_api | Tomcat started on port(s): 8080
  ```

---

### 4️⃣ Test the API

#### 🔹 Check database search
```bash
curl "http://localhost:8080/api/film-search?title=ACADEMY"
```
Expected JSON result (sample):
```json
[
  {
    "filmId":1,
    "title":"ACADEMY DINOSAUR",
    "description":"A Epic Drama of a Feminist And a Mad Scientist...",
    "releaseYear":"2006-01-01",
    "length":86
  }
]
```

#### 🔹 Test OCR → metadata lookup
```bash
curl -F "file=@test_movie_poster.png" http://localhost:8080/api/poster-info
```
Expected JSON result:
```json
{
  "releaseYear":"2006-01-01",
  "title":"ACADEMY DINOSAUR",
  "description":"A Epic Drama of a Feminist And a Mad Scientist..."
}
```

---

### 5️⃣ Stop the System
To stop everything safely:
```bash
docker compose down
```

To remove containers **and** reset the MySQL database:
```bash
docker compose down -v
```

---

## 🧩 Directory Structure

| Path | Description |
|------|--------------|
| `src/` | Java source code (Spring Boot app) |
| `sakila-db/` | SQL scripts to initialize the Sakila database |
| `Dockerfile` | Builds the self-contained Java + OCR image |
| `docker-compose.yml` | Defines `db` and `api` services |
| `test_movie_poster.png` | Sample movie poster for OCR testing |
| `README_Overview.md` | Detailed system design and usage notes |

---

## 💡 Notes

- **No manual setup** is needed for Java, MySQL, or Tesseract — Docker handles all dependencies.  
- If you see `Cannot connect to the Docker daemon`, make sure Docker Desktop is running.  
- First startup may take several minutes while images download.

---

## 🧱 Optional (for developers)

If you’d like to rebuild only the API container after code changes:
```bash
docker compose build api
docker compose up api
```

---

## ✅ Summary

| Step | Command |
|------|----------|
| Download ZIP | — |
| Unzip | `unzip movie_search_app-main.zip` |
| Run | `docker compose up --build` |
| Test Search | `curl "http://localhost:8080/api/film-search?title=ACADEMY"` |
| Test OCR | `curl -F "file=@test_movie_poster.png" http://localhost:8080/api/poster-info"` |
| Stop | `docker compose down` |
