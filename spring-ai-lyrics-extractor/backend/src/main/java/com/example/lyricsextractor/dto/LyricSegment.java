package com.example.lyricsextractor.dto;

public record LyricSegment(
        double start,
        double end,
        String text
) {
}
