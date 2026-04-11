package com.likelion.animalface.domain.analysis.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "동물상 분석 요청 DTO")
public record AnalyzeReq(
        @Schema(description = "분석할 이미지 URL", example = "https://bucket.s3.amazonaws.com/image.jpg")
        @NotBlank(message = "이미지 URL은 필수입니다.")
        @Pattern(regexp = "^https?://.*", message = "imageUrl은 http 또는 https로 시작해야 합니다.")
        String imageUrl
) {
}
