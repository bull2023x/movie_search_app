# 🎬 Movie Search Project (Phase 1 & 2)

This Movie Search Project is divided into two main phases. By completing these phases sequentially, you will:

- ✅ Build a Spring Boot REST API that searches the Sakila MySQL database for movies (Phase 1)
- ✅ Extend the service to accept image uploads, perform OCR on movie posters, and return movie details (Phase 2)

---

## 📦 Phase Definitions

### Phase 1: Basic Movie Search API
Create a Spring Boot application that exposes two endpoints against the Sakila database:

```http
GET /api/film-by-title?title=<TITLE>     # exact match
GET /api/film-search?title=<TITLE>       # partial match
```

### Phase 2: OCR Integration
Add image upload and OCR using Tess4J/Tesseract:

```http
POST /api/ocr           # raw OCR result
POST /api/poster-info   # OCR + movie search response
```

---

## ⚙️ Technical Requirements

| Component         | Version / Tool |
|------------------|----------------|
| OS               | Ubuntu 24.04 LTS (on OL9 KVM) |
| Java             | OpenJDK 17+ (tested on 21.0.7) |
| Spring Boot      | 3.5.0 |
| Maven            | 3.8+ |
| MySQL            | 8.x (with Sakila schema) |
| OCR Engine       | Tess4J 5.x + Tesseract 5.x |
| Server           | Embedded Tomcat 10.1.41 |
| Tools            | `curl`, `unzip`, `lsof`, `kill` |

---

## 🚀 What You Achieve

- After **Phase 1**: You have a working REST API to search movies in Sakila DB by title.
- After **Phase 2**: You support image upload → OCR → movie lookup pipeline.

---

## 🧩 Phase 1: Implementation Steps

### 1. Generate Spring Boot App

```bash
dir=~/movie-search
mkdir -p "$dir" && cd "$dir"

curl https://start.spring.io/starter.zip \
  -d dependencies=web,data-jpa \
  -d type=maven-project \
  -d language=java \
  -d javaVersion=17 \
  -d groupId=com.example \
  -d artifactId=sakila-api \
  -d name=sakila-api \
  -o sakila-api.zip

unzip sakila-api.zip && cd sakila-api
```

### 2. Set Up Sakila MySQL DB

```bash
unzip ~/sakila-db.zip -d ~/sakila-db

mysql -u root -p < ~/sakila-db/sakila-schema.sql
mysql -u root -p sakila < ~/sakila-db/sakila-data.sql
```

### 3. Configure `pom.xml`

Add:

```xml
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
```

### 4. Configure `application.properties`

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/sakila
spring.datasource.username=<DB_USER>
spring.datasource.password=<DB_PASS>
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
```

### 5. Implement Code

- `Film.java` → entity
- `FilmRepository.java` → JPA repo
- `FilmController.java` → controller

Sample endpoint:

```java
@GetMapping("/api/film-by-title")
public ResponseEntity<?> getByTitle(@RequestParam String title) {
  return ResponseEntity.ok(filmRepository.findByTitle(title));
}
```

### 6. Build & Run

```bash
./mvnw clean package
./mvnw spring-boot:run
```

### 7. Verify Endpoints

```bash
curl "http://localhost:8080/api/film-by-title?title=ACADEMY%20DINOSAUR"
curl "http://localhost:8080/api/film-search?title=terminator"
```

---

## 🔍 Phase 2: Add OCR Functionality

### 1. Add Tess4J Dependency to `pom.xml`

```xml
<dependency>
  <groupId>net.sourceforge.tess4j</groupId>
  <artifactId>tess4j</artifactId>
  <version>5.6.0</version>
</dependency>
```

### 2. Update `application.properties`

```properties
tess4j.datapath=/usr/share/tesseract-ocr/5/tessdata
```

### 3. Create `OcrController.java`

```java
@RestController
public class OcrController {

  @Value("${tess4j.datapath}")
  private String tessDataPath;

  private final FilmController filmController;

  public OcrController(FilmController filmController) {
    this.filmController = filmController;
  }

  @PostMapping("/api/ocr")
  public ResponseEntity<String> ocr(@RequestParam MultipartFile file) throws Exception {
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
```

### 4. Rebuild and Run

```bash
./mvnw clean package
./mvnw spring-boot:run
```

### 5. Test OCR + Movie Info

```bash
curl -X POST -F "file=@/home/your_user/poster.jpg" http://localhost:8080/api/ocr
curl -X POST -F "file=@/home/your_user/poster.jpg" http://localhost:8080/api/poster-info
```

---

## 🧠 Bonus: Plugin Integration (Advanced)

To enable plugin usage in ChatGPT:

1. Generate OpenAPI spec from your Spring controllers
2. Place `ai-plugin.json` + `openapi.json` in `/static` path
3. Expose API to network: e.g. `http://172.16.90.107:8080`
4. Register plugin in ChatGPT via manifest URL
5. Done!

You can now upload a poster and have ChatGPT respond with movie info!

---
