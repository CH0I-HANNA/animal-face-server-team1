package com.likelion.animalface.domain.user.controller;

import com.likelion.animalface.domain.user.dto.req.*;
import com.likelion.animalface.domain.user.dto.res.FindIdRes;
import com.likelion.animalface.domain.user.dto.res.LoginRes;
import com.likelion.animalface.domain.user.service.UserService;
import com.likelion.animalface.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    public ApiResponse<Void> signup(@RequestBody SignupReq req) {
        userService.signup(req);
        return ApiResponse.message("회원가입이 완료되었습니다.");
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ApiResponse<LoginRes> login(@RequestBody LoginReq req) {
        return ApiResponse.success(userService.login(req));
    }

    @Operation(summary = "토큰 재발급 (Rotation)")
    @PostMapping("/reissue")
    public ApiResponse<LoginRes> reissue(@RequestBody ReissueReq req) {
        return ApiResponse.success(userService.reissue(req));
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@AuthenticationPrincipal Long userId) {
        userService.logout(userId);
        return ApiResponse.message("로그아웃되었습니다.");
    }

    @Operation(summary = "아이디 찾기")
    @PostMapping("/find-id")
    public ApiResponse<FindIdRes> findId(@RequestBody FindIdReq req) {
        return ApiResponse.success(userService.findId(req));
    }

    @Operation(summary = "임시 비밀번호 발급 (이메일 발송)")
    @PostMapping("/temp-password")
    public ApiResponse<Void> tempPassword(@RequestBody TempPasswordReq req) {
        userService.sendTempPassword(req);
        return ApiResponse.message("임시 비밀번호가 이메일로 발송되었습니다.");
    }
}
