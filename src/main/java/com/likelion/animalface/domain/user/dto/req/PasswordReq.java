package com.likelion.animalface.domain.user.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "비밀번호 변경 요청 DTO")
public record PasswordReq(
        @Schema(description = "현재 비밀번호", example = "oldPassword123!")
        String currentPassword,

        @Schema(description = "새 비밀번호", example = "newPassword456!")
        String newPassword
) {
}
