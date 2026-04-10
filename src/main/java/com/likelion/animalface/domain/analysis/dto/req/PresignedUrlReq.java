package com.likelion.animalface.domain.analysis.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Presigned URL 발급 요청 DTO")
public record PresignedUrlReq(
        @Schema(description = "업로드할 파일명", example = "face.jpg")
        String fileName,

        @Schema(description = "파일 Content-Type", example = "image/jpeg")
        String contentType
) {
}
