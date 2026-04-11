package com.likelion.animalface.global.config;

import com.likelion.animalface.global.exception.AiServerException;
import feign.Logger;
import feign.Request;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class FeignConfig {

    /** Feign 로그 레벨 */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    /**
     * AI 서버 타임아웃 설정.
     * - connectTimeout: AI 서버에 연결 시도 최대 시간
     * - readTimeout: AI 응답 대기 최대 시간 (얼굴 분석에 시간이 걸리므로 넉넉하게)
     */
    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(
                3, TimeUnit.SECONDS,   // connectTimeout
                30, TimeUnit.SECONDS,  // readTimeout (AI 분석 시간 고려)
                false                  // followRedirects (false로 고정하여 SSRF 우회 공격 차단)
        );
    }

    /**
     * AI 서버 에러 응답을 AiServerException으로 변환합니다.
     * - 4xx: AI 서버가 우리 요청을 거부한 경우
     * - 5xx: AI 서버 내부 오류
     */
    @Bean
    public ErrorDecoder errorDecoder() {
        return (String methodKey, Response response) -> {
            int status = response.status();
            String message;

            if (status >= 400 && status < 500) {
                message = String.format(
                        "AI 서버가 요청을 거부했습니다. (HTTP %d) — 이미지 URL이 올바른지 확인하세요.", status);
            } else if (status >= 500) {
                message = String.format(
                        "AI 서버에 오류가 발생했습니다. (HTTP %d) — 잠시 후 다시 시도해주세요.", status);
            } else {
                message = String.format("AI 서버와 통신 중 예상치 못한 오류가 발생했습니다. (HTTP %d)", status);
            }

            return new AiServerException(message, status);
        };
    }
}
