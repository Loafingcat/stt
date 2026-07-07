package com.example.lyricsextractor.service;

import com.example.lyricsextractor.dto.CleanupRequest;
import com.example.lyricsextractor.dto.LyricSegment;
import com.example.lyricsextractor.dto.LyricsExtractResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Whisper가 반환한 원시 전사 결과를 Ollama로 후처리하는 서비스입니다.
 *
 * 역할:
 * - 줄바꿈 정리
 * - 반복구 정리
 * - 너무 이상한 문장 표시
 * - 최종 LyricsExtractResponse 생성
 */
@Service
public class OllamaCleanupService {

    private final ChatClient chatClient;

    public OllamaCleanupService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public LyricsExtractResponse cleanup(CleanupRequest request) {
        // TODO:
        //  1. request.segments()를 문자열로 변환한다.
        //  2. Ollama에게 "가사 형태로 정리해줘"라고 요청한다.
        //  3. 응답을 LyricsExtractResponse 형태로 변환한다.
        //
        // 지금은 아직 학습용이므로 Ollama 호출 대신 간단한 임시 응답을 반환한다.

        List<LyricSegment> cleanedLines = request.segments()
                .stream()
                .map(segment -> new LyricSegment(
                        segment.start(),
                        segment.end(),
                        segment.text().trim()
                ))
                .collect(Collectors.toList());

        return new LyricsExtractResponse(
                request.filename(),
                request.language(),
                cleanedLines,
                List.of("현재는 Ollama 후처리 로직이 TODO 상태입니다.")
        );
    }

    /**
     * 나중에 실제 Ollama 호출을 구현할 때 참고할 메서드입니다.
     *
     * 지금은 직접 사용하지 않습니다.
     */
    private String cleanupByOllama(CleanupRequest request) {
        String rawText = request.segments()
                .stream()
                .map(segment -> "[%.2f - %.2f] %s".formatted(
                        segment.start(),
                        segment.end(),
                        segment.text()
                ))
                .collect(Collectors.joining("\n"));

        String prompt = """
                다음은 노래 오디오에서 자동 전사한 원시 텍스트입니다.

                이 결과를 가사 초안 형태로 정리해 주세요.

                규칙:
                - 원문에 없는 내용을 새로 만들지 마세요.
                - 가사처럼 줄 단위로 정리하세요.
                - 타임스탬프는 유지하세요.
                - 알아듣기 어려운 부분은 [불확실]이라고 표시하세요.
                - 저작권 보호 가사를 정확한 공식 가사처럼 단정하지 마세요.
                - 자동 추출 결과라는 경고를 포함하세요.

                원시 전사 결과:
                %s
                """.formatted(rawText);

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
}