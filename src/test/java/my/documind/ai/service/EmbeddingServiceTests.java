package my.documind.ai.service;

import my.documind.ai.client.OpenAiClient;
import my.documind.ai.dto.EmbeddingResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EmbeddingServiceTests {
    @Mock
    private OpenAiClient openAiClient;

    @InjectMocks
    private EmbeddingService embeddingService;

    @Test
    @DisplayName("임베딩 API 호출 결과를 반환한다")
    void shouldReturnEmbedding_whenApiRequestSucceeds() {
        // given
        EmbeddingResponse response =
                new EmbeddingResponse(List.of(new EmbeddingResponse.EmbeddingData(List.of(0.1, -0.2, 0.3))));

        when(openAiClient.createEmbedding(anyString()))
                .thenReturn(response);

        // when
        float[] embedding = embeddingService.embed(anyString());

        // then
        assertThat(embedding).containsExactly(0.1f, -0.2f, 0.3f);
        verify(openAiClient).createEmbedding(anyString());
    }

    @Test
    @DisplayName("임베딩 API 응답이 없으면 예외가 발생한다")
    void shouldThrowException_whenApiResponseIsNull() {
        // given
        when(openAiClient.createEmbedding(anyString()))
                .thenReturn(null);

        // when & then
        assertThatThrownBy(() -> embeddingService.embed(anyString()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("임베딩 생성에 실패했습니다.");
    }

    @Test
    @DisplayName("임베딩 데이터가 없으면 예외가 발생한다")
    void shouldThrowException_whenEmbeddingDataIsNull() {
        // given
        EmbeddingResponse response = new EmbeddingResponse(null);

        when(openAiClient.createEmbedding(anyString()))
                .thenReturn(response);

        // when & then
        assertThatThrownBy(() -> embeddingService.embed(anyString()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("임베딩 생성에 실패했습니다.");
    }

    @Test
    @DisplayName("임베딩 데이터가 비어 있으면 예외가 발생한다")
    void shouldThrowException_whenEmbeddingDataIsEmpty() {
        // given
        EmbeddingResponse response = new EmbeddingResponse(List.of());

        when(openAiClient.createEmbedding(anyString()))
                .thenReturn(response);

        // when & then
        assertThatThrownBy(() -> embeddingService.embed(anyString()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("임베딩 생성에 실패했습니다.");
    }

    @Test
    @DisplayName("임베딩 값이 없으면 예외가 발생한다")
    void shouldThrowException_whenEmbeddingValuesAreNull() {
        // given
        EmbeddingResponse response = new EmbeddingResponse(List.of(new EmbeddingResponse.EmbeddingData(null)));

        when(openAiClient.createEmbedding(anyString()))
                .thenReturn(response);

        // when & then
        assertThatThrownBy(() -> embeddingService.embed(anyString()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("임베딩 생성에 실패했습니다.");
    }

    @Test
    @DisplayName("임베딩 값이 비어 있으면 예외가 발생한다")
    void shouldThrowException_whenEmbeddingValuesAreEmpty() {
        // given
        EmbeddingResponse response = new EmbeddingResponse(List.of(new EmbeddingResponse.EmbeddingData(List.of())));

        when(openAiClient.createEmbedding(anyString()))
                .thenReturn(response);

        // when & then
        assertThatThrownBy(() ->
                embeddingService.embed(anyString())
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("임베딩 생성에 실패했습니다.");
    }
}
