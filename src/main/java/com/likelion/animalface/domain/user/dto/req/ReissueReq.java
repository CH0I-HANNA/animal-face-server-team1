package com.likelion.animalface.domain.user.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "토큰 재발급 요청 DTO")
public record ReissueReq(
        @Schema(description = "리프레시 토큰")
        String refreshToken
) {
}