package com.likelion.animalface.domain.analysis.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Presigned URL 응답 DTO")
public record PresignedUrlRes(
        @Schema(description = "S3 업로드용 Presigned URL")
        String presignedUrl,

        @Schema(description = "업로드 후 접근 가능한 파일 URL")
        String imageUrl
) {
    public static PresignedUrlRes of(String presignedUrl, String fileUrl) {
        return new PresignedUrlRes(presignedUrl, fileUrl);
    }
}
