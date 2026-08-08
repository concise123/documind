package my.documind.ai.service;

import my.documind.ai.exception.EmptyContextException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ContextBuilderTests {
    private ContextBuilder contextBuilder;

    @BeforeEach
    void setUp() {
        contextBuilder = new ContextBuilder();
    }

    @Test
    @DisplayName("문서 내용을 정규화하여 반환한다")
    void shouldNormalizeWhitespace_whenContentContainsWhitespace()  {
        String content = "First   paragraph\n\n\n\n\nNext paragraph";
        String result = contextBuilder.build(content);
        assertThat(result).isEqualTo("First paragraph\n\nNext paragraph");
    }

    @Test
    @DisplayName("긴 문서는 AI 분석 가능한 형태로 변환한다")
    void shouldTruncateContent_whenContentExceedsMaxCharacters(){
        String content = "a".repeat(30000);
        String result = contextBuilder.build(content);
        assertThat(result.length()).isLessThan(content.length());
    }

    @Test
    @DisplayName("문서 내용이 없으면 AI 분석을 수행할 수 없다")
    void shouldThrowException_WhenContentIsEmpty() {
        // when & then
        assertThatThrownBy(() -> contextBuilder.build(" "))
                .isInstanceOf(EmptyContextException.class);
    }

    @Test
    @DisplayName("문서 내용이 없으면 AI 분석을 수행할 수 없다")
    void shouldThrowException_WhenContentIsNull() {
        // when & then
        assertThatThrownBy(() -> contextBuilder.build(null))
                .isInstanceOf(EmptyContextException.class);
    }
}
