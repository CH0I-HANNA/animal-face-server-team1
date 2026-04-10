package com.likelion.animalface.domain.user.service;

import com.likelion.animalface.domain.user.dto.req.*;
import com.likelion.animalface.domain.user.dto.res.FindIdRes;
import com.likelion.animalface.domain.user.dto.res.LoginRes;
import com.likelion.animalface.domain.user.entity.RefreshToken;
import com.likelion.animalface.domain.user.entity.User;
import com.likelion.animalface.domain.user.repository.RefreshTokenRepository;
import com.likelion.animalface.domain.user.repository.UserRepository;
import com.likelion.animalface.global.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 인증 서비스 (/api/v1/auth)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final JavaMailSender mailSender;

    /** 회원가입 */
    @Transactional
    public void signup(SignupReq req) {
        if (userRepository.existsByLoginId(req.loginId())) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }
        if (userRepository.existsByEmail(req.email())) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다.");
        }
        userRepository.save(req.toEntity(passwordEncoder.encode(req.password())));
    }

    /** 로그인 */
    @Transactional
    public LoginRes login(LoginReq req) {
        User user = userRepository.findByLoginId(req.loginId())
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        String accessToken = jwtProvider.generateAccessToken(user.getId());
        String refreshToken = jwtProvider.generateRefreshToken(user.getId());

        refreshTokenRepository.findByUserId(user.getId())
                .ifPresentOrElse(
                        rt -> rt.rotate(refreshToken, LocalDateTime.now().plusDays(14)),
                        () -> refreshTokenRepository.save(RefreshToken.builder()
                                .userId(user.getId())
                                .token(refreshToken)
                                .expiresAt(LocalDateTime.now().plusDays(14))
                                .build())
                );

        return LoginRes.of(accessToken, refreshToken);
    }

    /** 토큰 재발급 (Rotation) */
    @Transactional
    public LoginRes reissue(ReissueReq req) {
        if (!jwtProvider.validateToken(req.refreshToken())) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        RefreshToken saved = refreshTokenRepository.findByToken(req.refreshToken())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리프레시 토큰입니다."));

        if (saved.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("만료된 리프레시 토큰입니다.");
        }

        Long userId = jwtProvider.extractUserId(req.refreshToken());
        String newAccessToken = jwtProvider.generateAccessToken(userId);
        String newRefreshToken = jwtProvider.generateRefreshToken(userId);

        saved.rotate(newRefreshToken, LocalDateTime.now().plusDays(14));

        return LoginRes.of(newAccessToken, newRefreshToken);
    }

    /** 로그아웃 */
    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    /** 아이디 찾기 */
    public FindIdRes findId(FindIdReq req) {
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일로 가입된 사용자가 없습니다."));
        return FindIdRes.of(user.getLoginId());
    }

    /** 임시 비밀번호 발급 및 이메일 발송 */
    @Transactional
    public void sendTempPassword(TempPasswordReq req) {
        User user = userRepository.findByLoginIdAndEmail(req.loginId(), req.email())
                .orElseThrow(() -> new IllegalArgumentException("일치하는 회원 정보가 없습니다."));

        String tempPassword = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        user.updatePassword(passwordEncoder.encode(tempPassword));

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(user.getEmail());
        mail.setSubject("[AnimalFace] 임시 비밀번호 안내");
        mail.setText("임시 비밀번호: " + tempPassword + "\n로그인 후 반드시 비밀번호를 변경해주세요.");
        mailSender.send(mail);
    }
}
