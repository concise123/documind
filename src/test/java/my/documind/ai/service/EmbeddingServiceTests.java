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
        String text = "텍스트";

        EmbeddingResponse response =
                new EmbeddingResponse(List.of(new EmbeddingResponse.EmbeddingData(List.of(0.1, -0.2, 0.3))));

        when(openAiClient.createEmbedding(text))
                .thenReturn(response);

        // when
        float[] embedding = embeddingService.embed(text);

        // then
        assertThat(embedding).containsExactly(0.1f, -0.2f, 0.3f);
        verify(openAiClient).createEmbedding(anyString());
    }
}
