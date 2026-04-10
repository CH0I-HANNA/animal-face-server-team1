package com.likelion.animalface.domain.analysis.dto.res;

import com.likelion.animalface.domain.analysis.entity.AnimalResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "분석 결과 응답 DTO")
public record AnalysisRes(
        @Schema(description = "분석 결과 ID")
        Long id,

        @Schema(description = "동물 유형", example = "CAT")
        String animalType,

        @Schema(description = "유사도 (%)", example = "87.5")
        Double similarity,

        @Schema(description = "분석한 이미지 URL")
        String imageUrl,

        @Schema(description = "분석 일시")
        LocalDateTime createdAt
) {
    public static AnalysisRes from(AnimalResult result) {
        return new AnalysisRes(
                result.getId(),
                result.getAnimalType().name(),
                result.getSimilarity(),
                result.getImageUrl(),
                result.getCreatedAt()
        );
    }
}
