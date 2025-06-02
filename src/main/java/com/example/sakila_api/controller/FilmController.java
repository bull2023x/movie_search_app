package com.example.sakila_api.controller;

import com.example.sakila_api.entity.Film;
import com.example.sakila_api.repository.FilmRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// ← add this:
import java.util.List;

@RestController
public class FilmController {
    private final FilmRepository repo;

    public FilmController(FilmRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/api/film-by-title")
    public ResponseEntity<Film> getByTitle(@RequestParam String title) {
        return repo.findByTitle(title)
                   .map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/api/film-search")
    public ResponseEntity<List<Film>> searchByTitle(@RequestParam String title) {
        List<Film> results = repo.findByTitleContainingIgnoreCase(title);
        if (results.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(results);
    }
}

