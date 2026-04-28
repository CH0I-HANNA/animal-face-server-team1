package com.likelion.animalface.infra.ai.dto;

import java.util.Map;

public record AiAnalyzeRes(
        Map<String, Double> similarities
) {
}
