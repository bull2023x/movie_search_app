package com.example.sakila_api.controller;

import com.example.sakila_api.entity.Film;
import com.example.sakila_api.repository.FilmRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class OcrController {

    private final FilmRepository repo;

    public OcrController(FilmRepository repo) {
        this.repo = repo;
    }

    @PostMapping("/poster-info")
    public ResponseEntity<Map<String, Object>> uploadPoster(
            @RequestParam("file") MultipartFile file
    ) {
        try {
            // 1) write upload to a temp file
            Path tmp = Files.createTempFile("upload-", ".png");
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
            }

            // 2) run Tesseract CLI on it
            ProcessBuilder pb = new ProcessBuilder(
                "tesseract",
                tmp.toString(),
                "stdout",
                "-l", "eng"
            );
            Process p = pb.start();

            // 3) capture only stdout (ignore stderr warnings)
            String raw = new String(
                p.getInputStream().readAllBytes(),
                StandardCharsets.UTF_8
            ).trim();
            int exit = p.waitFor();

            // 4) delete temp file
            Files.deleteIfExists(tmp);

            if (exit != 0) {
                return ResponseEntity
                    .status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of(
                        "error",   "TesseractError",
                        "message", "Exit code " + exit
                    ));
            }

            // 5) strip out any warning lines – keep only last non-blank
            String[] lines = raw.split("\\r?\\n");
            String extracted = "";
            for (int i = lines.length - 1; i >= 0; i--) {
                if (!lines[i].isBlank()) {
                    extracted = lines[i].trim();
                    break;
                }
            }

            // 6) look up in the Sakila DB
            Optional<Film> opt = repo.findByTitle(extracted);
            if (opt.isEmpty()) {
                return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("title", extracted));
            }

            // 7) return only the 3 fields your Film entity has
            Film film = opt.get();
            return ResponseEntity.ok(Map.of(
                "title",       film.getTitle(),
                "releaseYear", film.getReleaseYear(),
                "description", film.getDescription()
            ));

        } catch (IOException | InterruptedException e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error",   e.getClass().getSimpleName(),
                    "message", e.getMessage()
                ));
        }
    }
}
