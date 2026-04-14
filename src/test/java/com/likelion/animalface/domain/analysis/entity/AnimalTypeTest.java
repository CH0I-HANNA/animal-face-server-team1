package com.likelion.animalface.domain.analysis.entity;

import com.likelion.animalface.global.exception.AiResponseParseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnimalTypeTest {

    @Test
    void 대문자_문자열로_변환_성공() {
        assertThat(AnimalType.from("CAT")).isEqualTo(AnimalType.CAT);
        assertThat(AnimalType.from("DOG")).isEqualTo(AnimalType.DOG);
        assertThat(AnimalType.from("FOX")).isEqualTo(AnimalType.FOX);
        assertThat(AnimalType.from("BEAR")).isEqualTo(AnimalType.BEAR);
    }

    @Test
    void 소문자_문자열로_변환_성공() {
        assertThat(AnimalType.from("cat")).isEqualTo(AnimalType.CAT);
        assertThat(AnimalType.from("dog")).isEqualTo(AnimalType.DOG);
    }

    @Test
    void 앞뒤_공백_포함_변환_성공() {
        assertThat(AnimalType.from("  fox  ")).isEqualTo(AnimalType.FOX);
    }

    @Test
    void null_입력시_예외발생() {
        assertThatThrownBy(() -> AnimalType.from(null))
                .isInstanceOf(AiResponseParseException.class)
                .hasMessageContaining("동물 타입을 반환하지 않았습니다");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  "})
    void 빈문자열_입력시_예외발생(String blank) {
        assertThatThrownBy(() -> AnimalType.from(blank))
                .isInstanceOf(AiResponseParseException.class)
                .hasMessageContaining("동물 타입을 반환하지 않았습니다");
    }

    @Test
    void 알수없는_타입_입력시_예외발생() {
        assertThatThrownBy(() -> AnimalType.from("RABBIT"))
                .isInstanceOf(AiResponseParseException.class)
                .hasMessageContaining("RABBIT");
    }
}
