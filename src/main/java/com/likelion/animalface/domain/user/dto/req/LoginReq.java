package com.likelion.animalface.domain.user.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 요청 DTO")
public record LoginReq(
        @Schema(description = "로그인 아이디", example = "likelion123")
        String loginId,

        @Schema(description = "비밀번호", example = "password123!")
        String password
) {
}