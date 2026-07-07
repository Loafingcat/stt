package com.example.lyricsextractor.dto;

import java.util.List;

public record LyricsExtractResponse(
        String filename,
        String language,
        List<LyricSegment> lines,
        List<String> warnings
) {
}