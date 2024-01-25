package com.example.ocr.controllers;

import com.example.ocr.services.ImageAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/crud/image-analysis")
@CrossOrigin(origins = "*")
public class ImageAnalysisController {

    @Autowired
    private ImageAnalysisService imageAnalysisService;

    @PostMapping(path = "/upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Object> upload(@RequestPart MultipartFile file) {
        return ResponseEntity.ok(this.imageAnalysisService.analyze(file));
    }

    @PostMapping(path = "/use-rest/upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Object> useRestUpload(@RequestPart MultipartFile file) {
        return ResponseEntity.ok(this.imageAnalysisService.analyzeUseRest(file));
    }
}
