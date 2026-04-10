package com.likelion.animalface.domain.analysis.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "동물상 분석 요청 DTO")
public record AnalyzeReq(
        @Schema(description = "분석할 이미지 URL", example = "https://bucket.s3.amazonaws.com/image.jpg")
        String imageUrl
) {
}
