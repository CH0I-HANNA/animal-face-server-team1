package com.likelion.animalface.domain.analysis.entity;

import com.likelion.animalface.global.exception.AiResponseParseException;

public enum AnimalType {
    CAT, DOG, FOX, BEAR;

    /**
     * AI 서버 응답 문자열을 AnimalType으로 변환합니다.
     * 대소문자를 무시하며, 알 수 없는 값이면 AiResponseParseException을 던집니다.
     *
     * @param value AI 서버가 반환한 animalType 문자열
     * @return 매칭되는 AnimalType
     * @throws AiResponseParseException 알 수 없는 동물 타입인 경우
     */
    public static AnimalType from(String value) {
        if (value == null || value.isBlank()) {
            throw new AiResponseParseException("AI 서버가 동물 타입을 반환하지 않았습니다.");
        }
        try {
            return AnimalType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AiResponseParseException(
                    "AI 서버에서 알 수 없는 동물 타입을 반환했습니다: \"" + value + "\". " +
                    "지원하는 타입: CAT, DOG, FOX, BEAR"
            );
        }
    }
}
