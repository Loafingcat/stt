package com.example.lyricsextractor.dto;
import java.util.List;


public record RawTranscriptionResponse(
        String filename,
        String language,
        List<LyricSegment> segments
) {
}
