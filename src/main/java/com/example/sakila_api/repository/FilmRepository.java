package com.example.sakila_api.repository;

import com.example.sakila_api.entity.Film;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FilmRepository extends JpaRepository<Film, Integer> {
    Optional<Film> findByTitle(String title);

    // ← new method
    List<Film> findByTitleContainingIgnoreCase(String title);
}

