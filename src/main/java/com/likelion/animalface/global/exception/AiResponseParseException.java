package com.likelion.animalface.global.exception;

/**
 * AI 서버 응답을 파싱할 수 없을 때 발생하는 예외.
 * 예) 알 수 없는 animalType 값 반환 시
 */
public class AiResponseParseException extends RuntimeException {

    public AiResponseParseException(String message) {
        super(message);
    }
}
