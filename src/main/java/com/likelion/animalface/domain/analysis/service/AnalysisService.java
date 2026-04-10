package com.likelion.animalface.domain.analysis.service;

import com.likelion.animalface.domain.analysis.dto.req.AnalyzeReq;
import com.likelion.animalface.domain.analysis.dto.res.AnalysisRes;
import com.likelion.animalface.domain.analysis.entity.AnimalResult;
import com.likelion.animalface.domain.analysis.entity.AnimalType;
import com.likelion.animalface.domain.analysis.repository.AnimalResultRepository;
import com.likelion.animalface.domain.user.entity.User;
import com.likelion.animalface.domain.user.repository.UserRepository;
import com.likelion.animalface.infra.ai.AiFeignClient;
import com.likelion.animalface.infra.ai.dto.AiAnalyzeReq;
import com.likelion.animalface.infra.ai.dto.AiAnalyzeRes;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 분석 서비스 (/api/v1/analysis)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalysisService {

    private final AnimalResultRepository animalResultRepository;
    private final UserRepository userRepository;
    private final AiFeignClient aiFeignClient;

    /** AI 분석 요청 후 결과 저장 */
    @Transactional
    public AnalysisRes analyze(Long userId, AnalyzeReq req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        AiAnalyzeRes aiRes = aiFeignClient.analyze(new AiAnalyzeReq(req.imageUrl()));

        AnimalResult result = AnimalResult.builder()
                .user(user)
                .imageUrl(req.imageUrl())
                .animalType(AnimalType.valueOf(aiRes.animalType().toUpperCase()))
                .similarity(aiRes.similarity())
                .build();

        return AnalysisRes.from(animalResultRepository.save(result));
    }

    /** 분석 결과 단건 조회 */
    public AnalysisRes getById(Long userId, Long id) {
        AnimalResult result = animalResultRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("분석 결과를 찾을 수 없습니다."));

        if (!result.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("접근 권한이 없습니다.");
        }

        return AnalysisRes.from(result);
    }

    /** 내 분석 기록 목록 (페이지네이션) */
    public Page<AnalysisRes> getMy(Long userId, Pageable pageable) {
        return animalResultRepository.findByUserId(userId, pageable)
                .map(AnalysisRes::from);
    }
}
