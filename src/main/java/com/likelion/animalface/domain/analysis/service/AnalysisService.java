package com.likelion.animalface.domain.analysis.service;

import com.likelion.animalface.domain.analysis.dto.req.AnalyzeReq;
import com.likelion.animalface.domain.analysis.dto.res.AnalysisRes;
import com.likelion.animalface.domain.analysis.entity.AnimalResult;
import com.likelion.animalface.domain.analysis.entity.AnimalType;
import com.likelion.animalface.domain.analysis.repository.AnimalResultRepository;
import com.likelion.animalface.domain.user.entity.User;
import com.likelion.animalface.domain.user.repository.UserRepository;
import com.likelion.animalface.global.exception.AiResponseParseException;
import com.likelion.animalface.global.exception.InvalidImageUrlException;
import com.likelion.animalface.infra.ai.AiFeignClient;
import com.likelion.animalface.infra.ai.dto.AiAnalyzeReq;
import com.likelion.animalface.infra.ai.dto.AiAnalyzeRes;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

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

    /** SSRF 방지: 허용된 S3 버킷 도메인 prefix를 동적으로 구성 */
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    /** AI 분석 요청 후 결과 저장 */
    @Transactional
    public AnalysisRes analyze(Long userId, AnalyzeReq req) {
        validateImageUrl(req.imageUrl());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        AiAnalyzeRes aiRes = aiFeignClient.analyze(new AiAnalyzeReq(req.imageUrl()));
        Map<String, Double> similarities = aiRes.similarities();

        validateSimilarities(similarities);

        Map.Entry<String, Double> top = similarities.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElseThrow(() -> new AiResponseParseException("AI 서버 유사도 결과가 비어 있습니다."));

        AnimalResult result = AnimalResult.builder()
                .user(user)
                .imageUrl(req.imageUrl())
                .animalType(AnimalType.from(top.getKey()))
                .similarity(top.getValue())
                .catSimilarity(similarities.getOrDefault(AnimalType.CAT.name(), 0.0))
                .dogSimilarity(similarities.getOrDefault(AnimalType.DOG.name(), 0.0))
                .foxSimilarity(similarities.getOrDefault(AnimalType.FOX.name(), 0.0))
                .bearSimilarity(similarities.getOrDefault(AnimalType.BEAR.name(), 0.0))
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

    private void validateSimilarities(Map<String, Double> similarities) {
        if (similarities == null || similarities.isEmpty()) {
            throw new AiResponseParseException("AI 서버가 유사도 결과를 반환하지 않았습니다.");
        }
    }

    /**
     * imageUrl이 자신의 S3 버킷에서 발급된 URL인지 검증합니다 (SSRF 방지).
     * StorageService가 생성하는 URL 형식: https://{bucket}.s3.{region}.amazonaws.com/...
     *
     * @throws InvalidImageUrlException 허용되지 않은 도메인의 URL인 경우
     */
    private void validateImageUrl(String imageUrl) {
        String allowedPrefix = String.format("https://%s.s3.%s.amazonaws.com/", bucket, region);
        if (!imageUrl.startsWith(allowedPrefix)) {
            throw new InvalidImageUrlException(
                    "허용되지 않은 이미지 URL입니다. 반드시 Presigned URL로 업로드된 이미지를 사용해야 합니다."
            );
        }
    }
}