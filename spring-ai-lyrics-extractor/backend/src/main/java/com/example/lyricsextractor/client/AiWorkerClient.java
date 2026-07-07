package com.example.lyricsextractor.client;

import com.example.lyricsextractor.dto.LyricSegment;
import com.example.lyricsextractor.dto.RawTranscriptionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Python ai-worker와 통신하는 클라이언트입니다.
 *
 * Spring Boot backend는 직접 Whisper를 실행하지 않습니다.
 * 대신 Python FastAPI 서버에 파일을 보내고,
 * 전사 결과를 JSON으로 돌려받습니다.
 */
@Component
public class AiWorkerClient {

    private final RestClient restClient;
    private final String aiWorkerUrl;

    public AiWorkerClient(
            RestClient.Builder restClientBuilder,
            @Value("${app.ai-worker.url}") String aiWorkerUrl
    ) {
        this.restClient = restClientBuilder.build();
        this.aiWorkerUrl = aiWorkerUrl;
    }

    public RawTranscriptionResponse transcribe(MultipartFile file) {
    return transcribeByHttp(file);
}
    /**
     * MultipartFile을 Python FastAPI 서버로 다시 전송합니다.
     *
     * 흐름:
     * 1. Spring이 받은 MultipartFile을 byte[]로 읽음
     * 2. ByteArrayResource로 감쌈
     * 3. multipart/form-data body 생성
     * 4. Python /transcribe로 POST 요청
     * 5. JSON 응답을 RawTranscriptionResponse로 매핑
     */
    private RawTranscriptionResponse transcribeByHttp(MultipartFile file) {
        try {
            ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
            bodyBuilder.part("file", fileResource)
                    .filename(file.getOriginalFilename())
                    .contentType(MediaType.parseMediaType(
                            file.getContentType() != null ? file.getContentType() : "application/octet-stream"
                    ));

            return restClient.post()
                    .uri(aiWorkerUrl + "/transcribe")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(bodyBuilder.build())
                    .retrieve()
                    .body(RawTranscriptionResponse.class);

        } catch (IOException e) {
            throw new IllegalArgumentException("파일을 읽는 중 오류가 발생했습니다.", e);
        }
    }
}