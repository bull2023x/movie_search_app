This is project to create app(Movie_searh), containze and distrubute without using Docker Hub.
Enjoy !!!!

 ✅ What You Will Do in This Project

## 1. Create the Movie_Search App (Spring Boot)   → Please refer to "README_detail1" for details.
You will build a REST API using Spring Boot that connects to the **MySQL Sakila** database.  
You’ll implement endpoints like:

- `GET /api/film-by-title?title=...`  
- `GET /api/film-search?title=...`

---

## 2. Containerize the App           → Please refer to "README_detail1" for details.
You will create a `Dockerfile` to package the Spring Boot application.  
Then, you'll write a `docker-compose.yml` file that:

- Spins up a **MySQL** container with the Sakila schema preloaded  
- Runs your **Spring Boot API** container connected to the DB

---

## 3. Run and Test the App          → Please refer to "README_detail1" for details.
You will:

- Start the containers locally (Linux or Windows)  
- Use `curl` or a browser to test that your API works  
- Ensure port settings (e.g., 8080, 3306) are correct  
- Troubleshoot firewall or VM network issues if needed

---

## 4. Share the App Without Docker Hub      → Please refer to "README_detail2" for details.
You’ll explore several ways to **share your containerized app**, even when Docker Hub is not allowed:

| Method | Description | Docker Hub Required |
|--------|-------------|----------------------|
| ✅ **Option 1** | Share as `.tar.gz` package using `docker save` and zip | ❌ |
| ✅ **Option 2** | Share source code via GitHub (with Docker setup) | ❌ |
| ❌ Docker Hub | Push to public registry | ✅ (If not allowed in your environment)

In this guide, we will follow **Option 1**: share as a zipped package (`.tar.gz`) that includes:

- `sakila-api.tar` (Docker image)
- `docker-compose.yml`
- `sakila-db/` folder (with SQL files)

---

## 5. Deploy the Shared App on Another Machine (e.g., Windows)
The recipient will:

1. Copy the `.tar.gz` file to their local environment  
2. Unzip it  
3. Run:

   ```bash
   docker load -i sakila-api.tar
   docker compose up -d
   ```

4. Confirm the app works at:  
   [http://localhost:8080/api/film-search?title=terminator](http://localhost:8080/api/film-search?title=terminator)
