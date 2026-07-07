package com.example.lyricsextractor.controller;

import com.example.lyricsextractor.dto.LyricsExtractResponse;
import com.example.lyricsextractor.service.LyricsService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/lyrics")
public class LyricsController {

    private final LyricsService lyricsService;

    public LyricsController(LyricsService lyricsService) {
        this.lyricsService = lyricsService;
    }

    @PostMapping(value = "/extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public LyricsExtractResponse extractLyrics(@RequestPart("file") MultipartFile file) {
        return lyricsService.extractLyrics(file);
    }
}
