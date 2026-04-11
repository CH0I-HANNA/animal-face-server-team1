package com.likelion.animalface.domain.analysis.controller;

import com.likelion.animalface.domain.analysis.dto.res.PokedexRes;
import com.likelion.animalface.domain.analysis.service.PokedexService;
import com.likelion.animalface.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Pokedex", description = "도감 API")
@RestController
@RequestMapping("/api/v1/pokedex")
@RequiredArgsConstructor
public class PokedexController {

    private final PokedexService pokedexService;

    @Operation(summary = "내 도감 조회", description = "본인이 발견한 모든 동물상과 획득 여부를 확인할 수 있습니다.")
    @GetMapping
    public ApiResponse<PokedexRes> getMyPokedex(@AuthenticationPrincipal Long userId) {
        return ApiResponse.success(pokedexService.getMyPokedex(userId));
    }
}
