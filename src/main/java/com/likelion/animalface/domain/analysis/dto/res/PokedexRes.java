package com.likelion.animalface.domain.analysis.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(description = "내 도감 전체 응답 DTO")
public record PokedexRes(
        @Schema(description = "현재까지 발견한 동물 종류 수", example = "3")
        int totalDiscovered,

        @Schema(description = "게임 내 전체 동물 종류 수", example = "10")
        int totalTypes,

        @Schema(description = "도감 엔트리 목록 (발견 여부 포함)")
        List<PokedexEntry> entries
) {
}
