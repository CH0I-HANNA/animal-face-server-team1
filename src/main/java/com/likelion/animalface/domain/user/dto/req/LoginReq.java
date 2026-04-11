package com.likelion.animalface.domain.user.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "로그인 요청 DTO")
public record LoginReq(
        @Schema(description = "로그인 아이디", example = "likelion123")
        @NotBlank(message = "아이디는 필수입니다.")
        String loginId,

        @Schema(description = "비밀번호", example = "password123!")
        @NotBlank(message = "비밀번호는 필수입니다.")
        String password
) {
}