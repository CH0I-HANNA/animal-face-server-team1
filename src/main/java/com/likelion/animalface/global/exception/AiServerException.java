package com.likelion.animalface.global.exception;

/**
 * AI 서버가 오류 응답(4xx/5xx)을 반환하거나 통신 자체가 실패했을 때 발생하는 예외.
 */
public class AiServerException extends RuntimeException {

    private final int status;

    public AiServerException(String message, int status) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
