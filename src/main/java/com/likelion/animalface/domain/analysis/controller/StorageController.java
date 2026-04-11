package com.likelion.animalface.domain.analysis.controller;

import com.likelion.animalface.domain.analysis.dto.req.PresignedUrlReq;
import com.likelion.animalface.domain.analysis.dto.res.PresignedUrlRes;
import com.likelion.animalface.domain.analysis.service.StorageService;
import com.likelion.animalface.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@Tag(name = "Storage", description = "스토리지 API")
@RestController
@RequestMapping("/api/v1/storage")
@RequiredArgsConstructor
public class StorageController {

    private final StorageService storageService;

    @Operation(summary = "S3 Presigned URL 발급", description = "파일명과 Content-Type으로 PUT용 Presigned URL을 발급합니다. 인증 필요.")
    @PostMapping("/presigned-url")
    public ApiResponse<PresignedUrlRes> getPresignedUrl(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody PresignedUrlReq req) {
        return ApiResponse.success(storageService.generatePresignedUrl(userId, req));
    }
}
