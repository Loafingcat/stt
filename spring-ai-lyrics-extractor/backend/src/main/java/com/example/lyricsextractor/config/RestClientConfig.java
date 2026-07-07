package com.example.lyricsextractor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * 외부 HTTP API를 호출하기 위한 RestClient 설정입니다.
 *
 * 여기서는 Spring Boot backend가 Python ai-worker를 호출할 때 사용합니다.
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}