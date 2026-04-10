package com.likelion.animalface.domain.user.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "아이디 찾기 응답 DTO")
public record FindIdRes(
        @Schema(description = "마스킹된 로그인 아이디", example = "li*****23")
        String maskedLoginId
) {
    public static FindIdRes of(String loginId) {
        return new FindIdRes(mask(loginId));
    }

    private static String mask(String loginId) {
        if (loginId.length() <= 4) {
            return loginId.charAt(0) + "***";
        }
        int showStart = 2;
        int showEnd = 2;
        String start = loginId.substring(0, showStart);
        String end = loginId.substring(loginId.length() - showEnd);
        String masked = "*".repeat(loginId.length() - showStart - showEnd);
        return start + masked + end;
    }
}
