package com.likelion.animalface.domain.analysis.dto.res;

import com.likelion.animalface.domain.analysis.entity.AnimalResult;
import com.likelion.animalface.domain.analysis.entity.AnimalType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "분석 결과 응답 DTO")
public record AnalysisRes(
        @Schema(description = "분석 결과 ID")
        Long id,

        @Schema(description = "대표 동물 유형 (가장 높은 확률)", example = "DOG")
        String animalType,

        @Schema(description = "대표 동물 유사도 (%)", example = "72.5")
        Double similarity,

        @Schema(description = "전체 동물 유형별 유사도 (%)", example = "{\"DOG\": 72.5, \"CAT\": 15.0, \"FOX\": 8.3, \"BEAR\": 4.2}")
        Map<String, Double> allSimilarities,

        @Schema(description = "분석한 이미지 URL")
        String imageUrl,

        @Schema(description = "분석 일시")
        LocalDateTime createdAt
) {
    public static AnalysisRes from(AnimalResult result) {
        Map<String, Double> allSimilarities = Map.of(
                AnimalType.CAT.name(), result.getCatSimilarity(),
                AnimalType.DOG.name(), result.getDogSimilarity(),
                AnimalType.FOX.name(), result.getFoxSimilarity(),
                AnimalType.BEAR.name(), result.getBearSimilarity()
        );
        return new AnalysisRes(
                result.getId(),
                result.getAnimalType().name(),
                result.getSimilarity(),
                allSimilarities,
                result.getImageUrl(),
                result.getCreatedAt()
        );
    }
}
