package com.likelion.animalface.domain.analysis.service;

import com.likelion.animalface.domain.analysis.dto.req.AnalyzeReq;
import com.likelion.animalface.domain.analysis.dto.res.AnalysisRes;
import com.likelion.animalface.domain.analysis.entity.AnimalResult;
import com.likelion.animalface.domain.analysis.entity.AnimalType;
import com.likelion.animalface.domain.analysis.repository.AnimalResultRepository;
import com.likelion.animalface.domain.user.entity.User;
import com.likelion.animalface.domain.user.repository.UserRepository;
import com.likelion.animalface.global.exception.InvalidImageUrlException;
import com.likelion.animalface.infra.ai.AiFeignClient;
import com.likelion.animalface.infra.ai.dto.AiAnalyzeReq;
import com.likelion.animalface.infra.ai.dto.AiAnalyzeRes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AnalysisServiceTest {

    @InjectMocks
    private AnalysisService analysisService;

    @Mock
    private AnimalResultRepository animalResultRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AiFeignClient aiFeignClient;

    private static final String BUCKET = "my-bucket";
    private static final String REGION = "ap-northeast-2";
    private static final String VALID_URL =
            "https://my-bucket.s3.ap-northeast-2.amazonaws.com/images/test.jpg";

    private static final Map<String, Double> SAMPLE_SIMILARITIES =
            Map.of("CAT", 88.0, "DOG", 7.0, "FOX", 3.0, "BEAR", 2.0);

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(analysisService, "bucket", BUCKET);
        ReflectionTestUtils.setField(analysisService, "region", REGION);
    }

    @Test
    void 분석_성공() {
        User user = User.builder().id(1L).loginId("user1").password("pw").nickname("nick").email("a@b.com").build();
        AnimalResult saved = AnimalResult.builder()
                .id(10L).user(user).imageUrl(VALID_URL)
                .animalType(AnimalType.CAT).similarity(88.0)
                .catSimilarity(88.0).dogSimilarity(7.0).foxSimilarity(3.0).bearSimilarity(2.0)
                .build();

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(aiFeignClient.analyze(any(AiAnalyzeReq.class))).willReturn(new AiAnalyzeRes(SAMPLE_SIMILARITIES));
        given(animalResultRepository.save(any())).willReturn(saved);

        AnalysisRes res = analysisService.analyze(1L, new AnalyzeReq(VALID_URL));

        assertThat(res.animalType()).isEqualTo("CAT");
        assertThat(res.similarity()).isEqualTo(88.0);
        assertThat(res.allSimilarities()).containsKeys("CAT", "DOG", "FOX", "BEAR");
    }

    @Test
    void 가장_높은_확률_동물이_대표로_선택된다() {
        User user = User.builder().id(1L).loginId("user1").password("pw").nickname("nick").email("a@b.com").build();
        Map<String, Double> dogTopSimilarities = Map.of("CAT", 10.0, "DOG", 75.0, "FOX", 9.0, "BEAR", 6.0);
        AnimalResult saved = AnimalResult.builder()
                .id(11L).user(user).imageUrl(VALID_URL)
                .animalType(AnimalType.DOG).similarity(75.0)
                .catSimilarity(10.0).dogSimilarity(75.0).foxSimilarity(9.0).bearSimilarity(6.0)
                .build();

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(aiFeignClient.analyze(any(AiAnalyzeReq.class))).willReturn(new AiAnalyzeRes(dogTopSimilarities));
        given(animalResultRepository.save(any())).willReturn(saved);

        AnalysisRes res = analysisService.analyze(1L, new AnalyzeReq(VALID_URL));

        assertThat(res.animalType()).isEqualTo("DOG");
        assertThat(res.similarity()).isEqualTo(75.0);
    }

    @Test
    void 허용되지않은_이미지URL이면_예외발생() {
        String externalUrl = "https://evil.com/hack.jpg";

        assertThatThrownBy(() -> analysisService.analyze(1L, new AnalyzeReq(externalUrl)))
                .isInstanceOf(InvalidImageUrlException.class);
    }

    @Test
    void 존재하지않는_유저면_예외발생() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> analysisService.analyze(99L, new AnalyzeReq(VALID_URL)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }

    @Test
    void ID로_분석결과_조회_성공() {
        User user = User.builder().id(1L).loginId("user1").password("pw").nickname("nick").email("a@b.com").build();
        AnimalResult result = AnimalResult.builder()
                .id(10L).user(user).imageUrl(VALID_URL)
                .animalType(AnimalType.DOG).similarity(75.0)
                .catSimilarity(10.0).dogSimilarity(75.0).foxSimilarity(9.0).bearSimilarity(6.0)
                .build();

        given(animalResultRepository.findById(10L)).willReturn(Optional.of(result));

        AnalysisRes res = analysisService.getById(1L, 10L);

        assertThat(res.id()).isEqualTo(10L);
        assertThat(res.animalType()).isEqualTo("DOG");
    }

    @Test
    void 분석결과_없으면_예외발생() {
        given(animalResultRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> analysisService.getById(1L, 999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("분석 결과를 찾을 수 없습니다");
    }

    @Test
    void 다른_유저의_분석결과_조회시_예외발생() {
        User owner = User.builder().id(1L).loginId("owner").password("pw").nickname("nick").email("a@b.com").build();
        AnimalResult result = AnimalResult.builder()
                .id(10L).user(owner).imageUrl(VALID_URL)
                .animalType(AnimalType.FOX).similarity(60.0)
                .catSimilarity(15.0).dogSimilarity(10.0).foxSimilarity(60.0).bearSimilarity(15.0)
                .build();

        given(animalResultRepository.findById(10L)).willReturn(Optional.of(result));

        assertThatThrownBy(() -> analysisService.getById(2L, 10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("접근 권한이 없습니다");
    }

    @Test
    void 내_분석기록_목록_페이지네이션() {
        User user = User.builder().id(1L).loginId("user1").password("pw").nickname("nick").email("a@b.com").build();
        AnimalResult r1 = AnimalResult.builder().id(1L).user(user).imageUrl(VALID_URL)
                .animalType(AnimalType.CAT).similarity(80.0)
                .catSimilarity(80.0).dogSimilarity(10.0).foxSimilarity(6.0).bearSimilarity(4.0)
                .build();
        AnimalResult r2 = AnimalResult.builder().id(2L).user(user).imageUrl(VALID_URL)
                .animalType(AnimalType.BEAR).similarity(70.0)
                .catSimilarity(10.0).dogSimilarity(12.0).foxSimilarity(8.0).bearSimilarity(70.0)
                .build();

        PageRequest pageable = PageRequest.of(0, 10);
        given(animalResultRepository.findByUserId(1L, pageable))
                .willReturn(new PageImpl<>(List.of(r1, r2)));

        Page<AnalysisRes> page = analysisService.getMy(1L, pageable);

        assertThat(page.getTotalElements()).isEqualTo(2);
        verify(animalResultRepository).findByUserId(1L, pageable);
    }
}