package com.likelion.animalface.global.advice;

import com.likelion.animalface.global.dto.ApiResponse;
import com.likelion.animalface.global.exception.AiResponseParseException;
import com.likelion.animalface.global.exception.AiServerException;
import com.likelion.animalface.global.exception.InvalidImageUrlException;
import feign.RetryableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /** @Valid 검증 실패: 어떤 필드가 왜 잘못됐는지 메시지에 포함 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<String>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(err -> "[" + err.getField() + "] " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
    }

    @ExceptionHandler(InvalidImageUrlException.class)
    public ResponseEntity<ApiResponse<String>> handleInvalidImageUrl(InvalidImageUrlException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)  // 400: 허용되지 않은 URL은 클라이언트 요청 오류
                .body(ApiResponse.error(e.getMessage()));
    }

    @ExceptionHandler(AiServerException.class)
    public ResponseEntity<ApiResponse<String>> handleAiServer(AiServerException e) {
        // AI 서버 5xx → 502, AI 서버 4xx → 400
        HttpStatus httpStatus = e.getStatus() >= 500
                ? HttpStatus.BAD_GATEWAY
                : HttpStatus.BAD_REQUEST;
        return ResponseEntity
                .status(httpStatus)
                .body(ApiResponse.error(e.getMessage()));
    }

    /** AI 서버 연결 실패 (Timeout, Connection Refused, DNS 등 포함) */
    @ExceptionHandler(RetryableException.class)
    public ResponseEntity<ApiResponse<String>> handleFeignRetryable(RetryableException e) {
        return ResponseEntity
                .status(HttpStatus.GATEWAY_TIMEOUT)  // 504
                .body(ApiResponse.error("AI 서버 통신 실패 (응답 지연 또는 연결 불가). 잠시 후 다시 시도해주세요."));
    }

    @ExceptionHandler(AiResponseParseException.class)
    public ResponseEntity<ApiResponse<String>> handleAiResponseParse(AiResponseParseException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)  // 502: 상위 서버(AI) 응답 문제
                .body(ApiResponse.error(e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<String>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<String>> handleIllegalState(IllegalStateException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGeneral(Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("서버 내부 오류가 발생했습니다."));
    }
}
