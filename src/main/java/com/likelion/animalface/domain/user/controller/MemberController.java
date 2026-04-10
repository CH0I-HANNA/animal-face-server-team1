package com.likelion.animalface.domain.user.controller;

import com.likelion.animalface.domain.user.dto.req.PasswordReq;
import com.likelion.animalface.domain.user.dto.res.MeRes;
import com.likelion.animalface.domain.user.service.MemberService;
import com.likelion.animalface.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Member", description = "회원 API")
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "내 정보 조회")
    @GetMapping("/me")
    public ApiResponse<MeRes> getMe(@AuthenticationPrincipal Long userId) {
        return ApiResponse.success(memberService.getMe(userId));
    }

    @Operation(summary = "비밀번호 변경")
    @PatchMapping("/me/password")
    public ApiResponse<Void> changePassword(@AuthenticationPrincipal Long userId,
                                             @RequestBody PasswordReq req) {
        memberService.changePassword(userId, req);
        return ApiResponse.message("비밀번호가 변경되었습니다.");
    }
}
