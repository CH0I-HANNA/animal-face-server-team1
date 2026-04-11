package com.likelion.animalface.domain.analysis.dto.res;

import com.likelion.animalface.domain.analysis.entity.AnimalType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@Schema(description = "단일 동물상 도감 엔트리 DTO")
public record PokedexEntry(
        @Schema(description = "동물 종류", example = "CAT")
        AnimalType animalType,

        @Schema(description = "발견 여부", example = "true")
        boolean discovered,

        @Schema(description = "역대 최고 일치도 (미발견시 null)", example = "92.5")
        Double bestSimilarity,

        @Schema(description = "최초 발견 일시 (미발견시 null)")
        LocalDateTime firstDiscoveredAt,

        @Schema(description = "최고 일치도를 기록한 사진 URL (미발견시 null)")
        String imageUrl
) {
    public static PokedexEntry undiscovered(AnimalType animalType) {
        return PokedexEntry.builder()
                .animalType(animalType)
                .discovered(false)
                .build();
    }
}
