package com.likelion.animalface.domain.user.service;

import com.likelion.animalface.domain.user.dto.req.FindIdReq;
import com.likelion.animalface.domain.user.dto.req.LoginReq;
import com.likelion.animalface.domain.user.dto.req.ReissueReq;
import com.likelion.animalface.domain.user.dto.req.SignupReq;
import com.likelion.animalface.domain.user.dto.res.FindIdRes;
import com.likelion.animalface.domain.user.dto.res.LoginRes;
import com.likelion.animalface.domain.user.entity.RefreshToken;
import com.likelion.animalface.domain.user.entity.User;
import com.likelion.animalface.domain.user.repository.RefreshTokenRepository;
import com.likelion.animalface.domain.user.repository.UserRepository;
import com.likelion.animalface.global.security.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        // refreshExpiryMs: 14일(ms)
        ReflectionTestUtils.setField(userService, "refreshExpiryMs", 1_209_600_000L);
    }

    // ────────────────── signup ──────────────────

    @Test
    void 회원가입_성공() {
        SignupReq req = new SignupReq("user1234", "password1!", "닉네임", "test@test.com");

        given(userRepository.existsByLoginId("user1234")).willReturn(false);
        given(userRepository.existsByEmail("test@test.com")).willReturn(false);
        given(passwordEncoder.encode("password1!")).willReturn("encoded");

        userService.signup(req);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void 중복_아이디로_회원가입시_예외발생() {
        SignupReq req = new SignupReq("user1234", "password1!", "닉네임", "test@test.com");
        given(userRepository.existsByLoginId("user1234")).willReturn(true);

        assertThatThrownBy(() -> userService.signup(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 존재하는 아이디");
    }

    @Test
    void 중복_이메일로_회원가입시_예외발생() {
        SignupReq req = new SignupReq("user1234", "password1!", "닉네임", "test@test.com");
        given(userRepository.existsByLoginId("user1234")).willReturn(false);
        given(userRepository.existsByEmail("test@test.com")).willReturn(true);

        assertThatThrownBy(() -> userService.signup(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 등록된 이메일");
    }

    // ────────────────── login ──────────────────

    @Test
    void 로그인_성공_기존_리프레시_토큰_Rotation() {
        User user = User.builder().id(1L).loginId("user1234").password("encoded").nickname("nick").email("a@b.com").build();
        RefreshToken existing = RefreshToken.builder().id(1L).userId(1L).token("old-rt").expiresAt(LocalDateTime.now().plusDays(14)).build();

        given(userRepository.findByLoginId("user1234")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("password1!", "encoded")).willReturn(true);
        given(jwtProvider.generateAccessToken(1L)).willReturn("new-at");
        given(jwtProvider.generateRefreshToken(1L)).willReturn("new-rt");
        given(refreshTokenRepository.findByUserId(1L)).willReturn(Optional.of(existing));

        LoginRes res = userService.login(new LoginReq("user1234", "password1!"));

        assertThat(res.accessToken()).isEqualTo("new-at");
        assertThat(res.refreshToken()).isEqualTo("new-rt");
    }

    @Test
    void 로그인_성공_리프레시_토큰_신규생성() {
        User user = User.builder().id(1L).loginId("user1234").password("encoded").nickname("nick").email("a@b.com").build();

        given(userRepository.findByLoginId("user1234")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("password1!", "encoded")).willReturn(true);
        given(jwtProvider.generateAccessToken(1L)).willReturn("new-at");
        given(jwtProvider.generateRefreshToken(1L)).willReturn("new-rt");
        given(refreshTokenRepository.findByUserId(1L)).willReturn(Optional.empty());

        LoginRes res = userService.login(new LoginReq("user1234", "password1!"));

        assertThat(res.accessToken()).isEqualTo("new-at");
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void 존재하지않는_아이디로_로그인시_예외발생() {
        given(userRepository.findByLoginId("nobody")).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login(new LoginReq("nobody", "pw")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("아이디 또는 비밀번호가 올바르지 않습니다");
    }

    @Test
    void 잘못된_비밀번호로_로그인시_예외발생() {
        User user = User.builder().id(1L).loginId("user1234").password("encoded").nickname("nick").email("a@b.com").build();
        given(userRepository.findByLoginId("user1234")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrong", "encoded")).willReturn(false);

        assertThatThrownBy(() -> userService.login(new LoginReq("user1234", "wrong")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("아이디 또는 비밀번호가 올바르지 않습니다");
    }

    // ────────────────── reissue ──────────────────

    @Test
    void 토큰_재발급_성공() {
        RefreshToken saved = RefreshToken.builder()
                .id(1L).userId(1L).token("valid-rt").expiresAt(LocalDateTime.now().plusDays(7)).build();

        given(jwtProvider.validateToken("valid-rt")).willReturn(true);
        given(refreshTokenRepository.findByToken("valid-rt")).willReturn(Optional.of(saved));
        given(jwtProvider.extractUserId("valid-rt")).willReturn(1L);
        given(jwtProvider.generateAccessToken(1L)).willReturn("new-at");
        given(jwtProvider.generateRefreshToken(1L)).willReturn("new-rt");

        LoginRes res = userService.reissue(new ReissueReq("valid-rt"));

        assertThat(res.accessToken()).isEqualTo("new-at");
        assertThat(res.refreshToken()).isEqualTo("new-rt");
    }

    @Test
    void 유효하지않은_리프레시토큰이면_예외발생() {
        given(jwtProvider.validateToken("bad-token")).willReturn(false);

        assertThatThrownBy(() -> userService.reissue(new ReissueReq("bad-token")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 리프레시 토큰");
    }

    @Test
    void DB에_없는_리프레시토큰이면_예외발생() {
        given(jwtProvider.validateToken("unknown-rt")).willReturn(true);
        given(refreshTokenRepository.findByToken("unknown-rt")).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.reissue(new ReissueReq("unknown-rt")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 리프레시 토큰");
    }

    @Test
    void 만료된_리프레시토큰이면_예외발생() {
        RefreshToken expired = RefreshToken.builder()
                .id(1L).userId(1L).token("expired-rt").expiresAt(LocalDateTime.now().minusDays(1)).build();

        given(jwtProvider.validateToken("expired-rt")).willReturn(true);
        given(refreshTokenRepository.findByToken("expired-rt")).willReturn(Optional.of(expired));

        assertThatThrownBy(() -> userService.reissue(new ReissueReq("expired-rt")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("만료된 리프레시 토큰");
    }

    // ────────────────── logout ──────────────────

    @Test
    void 로그아웃_성공() {
        userService.logout(1L);
        verify(refreshTokenRepository).deleteByUserId(1L);
    }

    // ────────────────── findId ──────────────────

    @Test
    void 이메일로_아이디찾기_성공() {
        User user = User.builder().id(1L).loginId("user1234").password("pw").nickname("nick").email("a@b.com").build();
        given(userRepository.findByEmail("a@b.com")).willReturn(Optional.of(user));

        FindIdRes res = userService.findId(new FindIdReq("a@b.com"));

        assertThat(res.maskedLoginId()).isEqualTo("us****34");
    }

    @Test
    void 가입되지않은_이메일로_아이디찾기시_예외발생() {
        given(userRepository.findByEmail("nobody@test.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findId(new FindIdReq("nobody@test.com")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("해당 이메일로 가입된 사용자가 없습니다");
    }
}