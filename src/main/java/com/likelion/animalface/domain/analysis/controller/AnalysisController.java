package com.likelion.animalface.domain.analysis.controller;

import com.likelion.animalface.domain.analysis.dto.req.AnalyzeReq;
import com.likelion.animalface.domain.analysis.dto.res.AnalysisRes;
import com.likelion.animalface.domain.analysis.service.AnalysisService;
import com.likelion.animalface.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Analysis", description = "동물상 분석 API")
@RestController
@RequestMapping("/api/v1/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    @Operation(summary = "동물상 분석", description = "이미지 URL을 AI 서버로 전달하여 분석 결과를 저장합니다.")
    @PostMapping
    public ApiResponse<AnalysisRes> analyze(@AuthenticationPrincipal Long userId,
                                             @RequestBody AnalyzeReq req) {
        return ApiResponse.success(analysisService.analyze(userId, req));
    }

    @Operation(summary = "분석 결과 상세 조회")
    @GetMapping("/{id}")
    public ApiResponse<AnalysisRes> getById(@AuthenticationPrincipal Long userId,
                                             @PathVariable Long id) {
        return ApiResponse.success(analysisService.getById(userId, id));
    }

    @Operation(summary = "내 분석 기록 목록 (페이지네이션)")
    @GetMapping("/my")
    public ApiResponse<Page<AnalysisRes>> getMy(
            @AuthenticationPrincipal Long userId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.success(analysisService.getMy(userId, pageable));
    }
}
