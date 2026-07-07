package com.example.lyricsextractor.service;

import com.example.lyricsextractor.client.AiWorkerClient;
import com.example.lyricsextractor.dto.CleanupRequest;
import com.example.lyricsextractor.dto.LyricsExtractResponse;
import com.example.lyricsextractor.dto.RawTranscriptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

/**
 * 가사 추출 전체 흐름을 담당하는 서비스입니다.
 *
 * Controller는 요청만 받고,
 * 실제 처리 흐름은 Service에서 관리합니다.
 */
@Service
public class LyricsService {

    private static final Set<String> ALLOWED_AUDIO_TYPES = Set.of(
        "audio/mpeg",
        "audio/mp3",
        "audio/wav",
        "audio/x-wav",
        "audio/wave",
        "audio/mp4",
        "audio/x-m4a",
        "audio/m4a"
);

    private final AiWorkerClient aiWorkerClient;
    private final OllamaCleanupService ollamaCleanupService;

    public LyricsService(
            AiWorkerClient aiWorkerClient,
            OllamaCleanupService ollamaCleanupService
    ) {
        this.aiWorkerClient = aiWorkerClient;
        this.ollamaCleanupService = ollamaCleanupService;
    }

    public LyricsExtractResponse extractLyrics(MultipartFile file) {
        validateAudioFile(file);

        // 1. Python AI Worker에 파일을 보내서 원시 전사 결과를 받는다.
        RawTranscriptionResponse rawResponse = aiWorkerClient.transcribe(file);

        // 2. Ollama 후처리용 요청 DTO로 변환한다.
        CleanupRequest cleanupRequest = new CleanupRequest(
                rawResponse.filename(),
                rawResponse.language(),
                rawResponse.segments()
        );

        // 3. Ollama 후처리 서비스를 호출해 최종 응답을 만든다.
        return ollamaCleanupService.cleanup(cleanupRequest);
    }

    private void validateAudioFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "오디오 파일이 필요합니다."
            );
        }

        String contentType = file.getContentType();

        if (contentType == null || !ALLOWED_AUDIO_TYPES.contains(contentType)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "mp3 또는 wav 오디오 파일만 지원합니다. 받은 타입: " + contentType
            );
        }
    }
}