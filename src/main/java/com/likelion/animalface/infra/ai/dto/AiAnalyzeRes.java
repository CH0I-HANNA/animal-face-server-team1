package com.likelion.animalface.infra.ai.dto;

public record AiAnalyzeRes(
        String animalType,
        Double similarity
) {
}
