package my.documind.service;

import lombok.extern.log4j.Log4j2;
import my.documind.client.OpenAiClient;
import my.documind.domain.*;
import my.documind.dto.SummaryResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Log4j2
@ExtendWith(MockitoExtension.class)
class SummaryServiceTests {
    @Mock
    private OpenAiClient openAiClient;

    @InjectMocks
    private SummaryService summaryService;

    @Test
    @DisplayName("문서를 AI로 요약하고 결과를 반환한다")
    void generateSummary_success() {
        // given
        Document document = Document.builder()
                .extractedText("원본 텍스트")
                .build();

        when(openAiClient.summarize(anyString())).thenReturn(new SummaryResponse("요약 결과", "gpt-4o-mini", 10));

        // when
        SummaryResponse response = summaryService.generateSummary(document);

        // then
        assertThat(response.content()).isEqualTo("요약 결과");
        verify(openAiClient).summarize(anyString());
    }
}
