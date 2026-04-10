package com.likelion.animalface.domain.user.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "아이디 찾기 요청 DTO")
public record FindIdReq(
        @Schema(description = "가입 시 등록한 이메일", example = "lion@likelion.org")
        String email
) {
}
