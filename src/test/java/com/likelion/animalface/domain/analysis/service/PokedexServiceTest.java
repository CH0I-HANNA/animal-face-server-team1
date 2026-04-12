package com.likelion.animalface.domain.analysis.service;

import com.likelion.animalface.domain.analysis.dto.res.PokedexEntry;
import com.likelion.animalface.domain.analysis.dto.res.PokedexRes;
import com.likelion.animalface.domain.analysis.entity.AnimalResult;
import com.likelion.animalface.domain.analysis.entity.AnimalType;
import com.likelion.animalface.domain.analysis.repository.AnimalResultRepository;
import com.likelion.animalface.domain.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PokedexServiceTest {

    @InjectMocks
    private PokedexService pokedexService;

    @Mock
    private AnimalResultRepository animalResultRepository;

    private static final User USER = User.builder()
            .id(1L).loginId("user1").password("pw").nickname("nick").email("a@b.com").build();

    private static final String IMAGE_URL = "https://bucket.s3.ap-northeast-2.amazonaws.com/img.jpg";

    @Test
    void 분석기록이_없으면_모두_미발견_상태() {
        given(animalResultRepository.findByUserId(1L, Pageable.unpaged()))
                .willReturn(new PageImpl<>(List.of()));

        PokedexRes res = pokedexService.getMyPokedex(1L);

        assertThat(res.totalDiscovered()).isEqualTo(0);
        assertThat(res.totalTypes()).isEqualTo(AnimalType.values().length);
        assertThat(res.entries()).allMatch(e -> !e.discovered());
    }

    @Test
    void 발견한_동물상은_discovered_true() {
        AnimalResult catResult = buildResult(AnimalType.CAT, 85.0, LocalDateTime.now().minusDays(1));

        given(animalResultRepository.findByUserId(1L, Pageable.unpaged()))
                .willReturn(new PageImpl<>(List.of(catResult)));

        PokedexRes res = pokedexService.getMyPokedex(1L);

        assertThat(res.totalDiscovered()).isEqualTo(1);

        PokedexEntry catEntry = res.entries().stream()
                .filter(e -> e.animalType() == AnimalType.CAT)
                .findFirst().orElseThrow();

        assertThat(catEntry.discovered()).isTrue();
        assertThat(catEntry.bestSimilarity()).isEqualTo(85.0);
        assertThat(catEntry.imageUrl()).isEqualTo(IMAGE_URL);
    }

    @Test
    void 동일_타입_여러_기록중_최고_유사도_선택() {
        AnimalResult low = buildResult(AnimalType.DOG, 60.0, LocalDateTime.now().minusDays(5));
        AnimalResult high = buildResult(AnimalType.DOG, 95.0, LocalDateTime.now().minusDays(2));

        given(animalResultRepository.findByUserId(1L, Pageable.unpaged()))
                .willReturn(new PageImpl<>(List.of(low, high)));

        PokedexRes res = pokedexService.getMyPokedex(1L);

        PokedexEntry dogEntry = res.entries().stream()
                .filter(e -> e.animalType() == AnimalType.DOG)
                .findFirst().orElseThrow();

        assertThat(dogEntry.bestSimilarity()).isEqualTo(95.0);
    }

    @Test
    void 동일_타입_여러_기록중_최초_발견일_선택() {
        LocalDateTime earliest = LocalDateTime.now().minusDays(10);
        LocalDateTime latest = LocalDateTime.now().minusDays(1);

        AnimalResult first = buildResult(AnimalType.FOX, 70.0, earliest);
        AnimalResult second = buildResult(AnimalType.FOX, 80.0, latest);

        given(animalResultRepository.findByUserId(1L, Pageable.unpaged()))
                .willReturn(new PageImpl<>(List.of(second, first)));

        PokedexRes res = pokedexService.getMyPokedex(1L);

        PokedexEntry foxEntry = res.entries().stream()
                .filter(e -> e.animalType() == AnimalType.FOX)
                .findFirst().orElseThrow();

        assertThat(foxEntry.firstDiscoveredAt()).isEqualTo(earliest);
    }

    @Test
    void 모든_타입_발견시_totalDiscovered가_전체_타입수와_동일() {
        List<AnimalResult> allTypes = List.of(
                buildResult(AnimalType.CAT, 80.0, LocalDateTime.now()),
                buildResult(AnimalType.DOG, 70.0, LocalDateTime.now()),
                buildResult(AnimalType.FOX, 90.0, LocalDateTime.now()),
                buildResult(AnimalType.BEAR, 60.0, LocalDateTime.now())
        );

        given(animalResultRepository.findByUserId(1L, Pageable.unpaged()))
                .willReturn(new PageImpl<>(allTypes));

        PokedexRes res = pokedexService.getMyPokedex(1L);

        assertThat(res.totalDiscovered()).isEqualTo(res.totalTypes());
        assertThat(res.entries()).allMatch(PokedexEntry::discovered);
    }

    private AnimalResult buildResult(AnimalType type, Double similarity, LocalDateTime createdAt) {
        AnimalResult result = AnimalResult.builder()
                .user(USER)
                .imageUrl(IMAGE_URL)
                .animalType(type)
                .similarity(similarity)
                .build();
        ReflectionTestUtils.setField(result, "createdAt", createdAt);
        return result;
    }
}
