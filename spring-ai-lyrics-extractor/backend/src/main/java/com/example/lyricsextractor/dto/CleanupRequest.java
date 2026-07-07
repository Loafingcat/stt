package com.example.lyricsextractor.dto;

import java.util.List;


public record CleanupRequest(
        String filename,
        String language,
        List<LyricSegment> segments
) {
}
