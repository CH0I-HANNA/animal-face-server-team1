package com.likelion.animalface.domain.user.controller;

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

}
