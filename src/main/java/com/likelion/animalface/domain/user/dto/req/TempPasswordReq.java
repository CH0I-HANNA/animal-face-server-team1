package com.likelion.animalface.domain.user.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "임시 비밀번호 발급 요청 DTO")
public record TempPasswordReq(
        @Schema(description = "로그인 아이디", example = "likelion123")
        String loginId,

        @Schema(description = "가입 시 등록한 이메일", example = "lion@likelion.org")
        String email
) {
}
