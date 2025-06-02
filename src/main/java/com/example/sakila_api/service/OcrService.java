// src/main/java/com/example/sakila_api/service/OcrService.java
package com.example.sakila_api.service;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;

@Service
public class OcrService {

    private final ITesseract tesseract;

    public OcrService(@Value("${tess4j.datapath}") String tessDataPath) {
        Tesseract engine = new Tesseract();
        engine.setDatapath(tessDataPath);          // path to your tessdata folder
        engine.setLanguage("eng");                 // use English traineddata
        this.tesseract = engine;
    }

    /**
     * Run OCR on a BufferedImage and return the extracted text.
     */
    public String extractText(BufferedImage image) {
        try {
            return tesseract.doOCR(image);
        } catch (TesseractException e) {
            throw new RuntimeException("Failed to OCR image", e);
        }
    }
}

