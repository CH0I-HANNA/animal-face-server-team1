package com.likelion.animalface.domain.analysis.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Presigned URL 발급 요청 DTO")
public record PresignedUrlReq(
        @Schema(description = "업로드할 파일명", example = "face.jpg")
        @NotBlank(message = "파일명은 필수입니다.")
        String fileName,

        @Schema(description = "파일 Content-Type", example = "image/jpeg")
        @NotBlank(message = "Content-Type은 필수입니다.")
        @Pattern(
                regexp = "(?i)^image/(jpeg|jpg|png|webp|gif)$",
                message = "이미지 파일만 업로드 가능합니다. (jpeg, jpg, png, webp, gif)"
        )
        String contentType
) {
}
