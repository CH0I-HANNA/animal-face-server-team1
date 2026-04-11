package com.likelion.animalface.global.exception;

/**
 * 허용되지 않은 이미지 URL이 요청됐을 때 발생하는 예외.
 * 자신의 S3 버킷 도메인이 아닌 외부 URL 차단(SSRF 방지) 목적.
 */
public class InvalidImageUrlException extends RuntimeException {

    public InvalidImageUrlException(String message) {
        super(message);
    }
}
