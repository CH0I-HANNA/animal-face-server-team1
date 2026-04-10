package com.likelion.animalface.domain.user.dto.res;

import com.likelion.animalface.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 정보 응답 DTO")
public record MeRes(
        @Schema(description = "로그인 아이디", example = "likelion123")
        String loginId,

        @Schema(description = "닉네임", example = "멋사사자")
        String nickname,

        @Schema(description = "이메일", example = "lion@likelion.org")
        String email
) {
    public static MeRes from(User user) {
        return new MeRes(user.getLoginId(), user.getNickname(), user.getEmail());
    }
}
