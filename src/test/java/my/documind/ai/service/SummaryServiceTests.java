package my.documind.ai.service;

import my.documind.ai.client.OpenAiClient;
import my.documind.ai.dto.SummaryResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SummaryServiceTests {
    @Mock
    private OpenAiClient openAiClient;

    @InjectMocks
    private SummaryService summaryService;

    @Test
    @DisplayName("문서 내용으로 요약 결과를 생성한다")
    void shouldCreateSummaryResult_whenDocumentContentExists() {
        // given
        String extractedText = "원본 텍스트";

        when(openAiClient.summarize(anyString()))
                .thenReturn(new SummaryResponse("요약 결과", "gpt-4o-mini", 10));

        // when
        SummaryResponse response = summaryService.generateSummary(extractedText);

        // then
        assertThat(response.content()).isEqualTo("요약 결과");
        verify(openAiClient).summarize(anyString());
    }
}
