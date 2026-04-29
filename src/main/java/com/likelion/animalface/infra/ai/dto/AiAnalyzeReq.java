package com.likelion.animalface.infra.ai.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

public record AiAnalyzeReq(
        @JsonProperty("image_url")
        String imageUrl
) {
}