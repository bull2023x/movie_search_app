
# How to Share and Deploy the Sakila API Container System

There are several options for sharing your Docker-based container system with colleagues or deploying it on other machines. This guide describes multiple methods, including direct file transfer, local container registry, and GitHub source sharing.

---

## ✨ Summary

This project provides a movie search API that:

* Extracts a movie title from a poster image using OCR
* Searches a MySQL Sakila database by that title
* Returns movie information via a REST API

---

## ✅ Option 1: Share Prebuilt Container via `.tar` File (No Docker Hub)

### Step 1: Save the Docker Image

```bash
cd ~/your-project-folder/sakila-api

docker save -o sakila-api.tar yourID/sakila-api:latest
```

### Step 2: Package the Image + Data + Compose File

```bash
tar czvf sakila-api-package.tar.gz \
  sakila-api.tar \
  docker-compose.yml \
  sakila-db/
```

Now you can send `sakila-api-package.tar.gz` to colleagues via file transfer, USB, or company file share.

### Step 3: Load and Run on Another Machine

1. Extract the archive:

```bash
tar xzvf sakila-api-package.tar.gz
```

2. Load Docker image:

```bash
docker load -i sakila-api.tar
```

3. Start the services:

```bash
docker compose up -d
```

---

## ✅ Option 2: Share Source via GitHub

You can also upload the following to GitHub:

* `Dockerfile`
* `docker-compose.yml`
* `sakila-db/` folder
* Full Spring Boot source under `src/`
* `pom.xml`, `.mvn/`, `mvnw`, `README.md`

### Usage on New Machine

```bash
git clone https://github.com/your-org/sakila-api.git
cd sakila-api
./mvnw package

# or build using Docker
sudo docker build -t yourID/sakila-api:latest .
docker compose up -d
```

---

## ✅ Option 3: Local Docker Registry (Advanced, Offline Repo)

If your company blocks Docker Hub but allows internal registries:

1. Run a local registry:

```bash
docker run -d -p 5000:5000 --restart always --name registry registry:2
```

2. Tag and push:

```bash
docker tag yourID/sakila-api localhost:5000/sakila-api

docker push localhost:5000/sakila-api
```

3. Pull on other machines:

```bash
docker pull localhost:5000/sakila-api
```

---

## ⚖️ Port Conflicts: Kill Processes on 8080/3306

Sometimes port 8080 (Tomcat) or 3306 (MySQL) is in use. Use:

```bash
sudo lsof -i:8080
sudo lsof -i:3306

# Kill the process
sudo kill -9 <PID>
```

---

## ✨ DONE: Once container is running...

You can verify:

```bash
curl http://localhost:8080/api/film-by-title?title=ACADEMY%20DINOSAUR
```

---

## 📄 Appendix: Firewall (if needed)

On Oracle Linux 9 host (OL9):

```bash
sudo firewall-cmd --add-port=8080/tcp --zone=public --permanent
sudo firewall-cmd --reload
```

---

## ℹ️ Notes

* `yourID` is a placeholder for your Docker Hub or internal tag
* Avoid using Docker Hub if prohibited by company policy
* Use bridged network mode in KVM or VirtualBox to allow access from host

---

## 📅 Prepared by: Sakila Movie API Team
