package com.example.sakila_api.controller;

import com.example.sakila_api.entity.Film;
import com.example.sakila_api.repository.FilmRepository;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
public class OcrController {

    private final Tesseract tesseract = new Tesseract();
    private final FilmRepository repo;

    public OcrController(
        FilmRepository repo,
        @Value("${tess4j.datapath}") String tessDataPath
    ) {
        this.repo = repo;
        tesseract.setDatapath(tessDataPath);
    }

    @PostMapping("/api/poster-info")
    public ResponseEntity<?> posterInfo(@RequestParam("file") MultipartFile file) {
        try {
            BufferedImage img = ImageIO.read(file.getInputStream());
            String rawText = tesseract.doOCR(img);

            // split OCR text into lines, try each line as a title query
            for (String line : rawText.split("\\R")) {
                String candidate = line.trim();
                if (candidate.length() < 3) continue;
                List<Film> matches = repo.findByTitleContainingIgnoreCase(candidate);
                if (!matches.isEmpty()) {
                    // return the first match (or you could return the whole list)
                    return ResponseEntity.ok(matches.get(0));
                }
            }
            // no match found
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body("No film found for OCR text:\n" + rawText);

        } catch (IOException | TesseractException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("OCR error: " + e.getMessage());
        }
    }
}

