package com.likelion.animalface.domain.user.dto.req;

import com.likelion.animalface.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원가입 요청 DTO")
public record SignupReq(
        @Schema(description = "로그인 아이디", example = "likelion123")
        String loginId,

        @Schema(description = "비밀번호", example = "password123!")
        String password,

        @Schema(description = "닉네임", example = "멋사사자")
        String nickname,

        @Schema(description = "이메일", example = "lion@likelion.org")
        String email
) {
    public User toEntity(String encodedPassword) {
        return User.builder()
                .loginId(loginId)
                .password(encodedPassword)
                .nickname(nickname)
                .email(email)
                .build();
    }
}
