package com.likelion.animalface.domain.analysis.service;

import com.likelion.animalface.domain.analysis.dto.res.PokedexEntry;
import com.likelion.animalface.domain.analysis.dto.res.PokedexRes;
import com.likelion.animalface.domain.analysis.entity.AnimalResult;
import com.likelion.animalface.domain.analysis.entity.AnimalType;
import com.likelion.animalface.domain.analysis.repository.AnimalResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 도감(Pokedex) 비즈니스 로직
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PokedexService {

    private final AnimalResultRepository animalResultRepository;

    /**
     * 사용자의 모든 동물상 분석 기록을 조회하여 도감 형태로 반환합니다.
     */
    public PokedexRes getMyPokedex(Long userId) {
        // 1. 유저의 모든 분석 기록 조회
        List<AnimalResult> results = animalResultRepository.findByUserId(userId, org.springframework.data.domain.Pageable.unpaged()).getContent();
        
        // 2. AnimalType 별로 그룹핑
        Map<AnimalType, List<AnimalResult>> groupedResults = results.stream()
                .collect(Collectors.groupingBy(AnimalResult::getAnimalType));

        // 3. 전체 AnimalType 순회하며 도감 엔트리 생성
        List<PokedexEntry> entries = Arrays.stream(AnimalType.values())
                .map(type -> {
                    List<AnimalResult> typeResults = groupedResults.get(type);

                    // 기록이 없는 경우
                    if (typeResults == null || typeResults.isEmpty()) {
                        return PokedexEntry.undiscovered(type);
                    }

                    // 기록이 있는 경우: 가장 유사도가 높은 기록 추출
                    AnimalResult bestResult = typeResults.stream()
                            .max(Comparator.comparing(AnimalResult::getSimilarity))
                            .orElseThrow();

                    // 최초 발견일(가장 오래된 기록) 추출
                    AnimalResult firstResult = typeResults.stream()
                            .min(Comparator.comparing(AnimalResult::getCreatedAt))
                            .orElseThrow();

                    return PokedexEntry.builder()
                            .animalType(type)
                            .discovered(true)
                            .bestSimilarity(bestResult.getSimilarity())
                            .firstDiscoveredAt(firstResult.getCreatedAt())
                            .imageUrl(bestResult.getImageUrl())
                            .build();
                })
                .collect(Collectors.toList());

        // 4. 발견한 동물 종류 수 계산
        int totalDiscovered = (int) entries.stream().filter(PokedexEntry::discovered).count();

        return PokedexRes.builder()
                .totalDiscovered(totalDiscovered)
                .totalTypes(AnimalType.values().length)
                .entries(entries)
                .build();
    }
}
