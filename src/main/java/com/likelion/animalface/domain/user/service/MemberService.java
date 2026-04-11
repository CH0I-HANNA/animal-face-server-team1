package com.likelion.animalface.domain.user.service;

import com.likelion.animalface.domain.user.dto.res.MeRes;
import com.likelion.animalface.domain.user.entity.User;
import com.likelion.animalface.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원 서비스 (/api/v1/members)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final UserRepository userRepository;

    /** 내 정보 조회 */
    public MeRes getMe(Long userId) {
        User user = findUser(userId);
        return MeRes.from(user);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }
}
