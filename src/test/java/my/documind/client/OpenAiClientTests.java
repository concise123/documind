package my.documind.client;

import lombok.extern.log4j.Log4j2;
import my.documind.dto.OpenAiRequest;
import my.documind.dto.OpenAiResponse;
import my.documind.dto.SummaryResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Log4j2
@Tag("integration")
@SpringBootTest
class OpenAiClientTests {
   @Mock
    private RestClient restClient;

   @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private OpenAiClient openAiClient;

    @Test
    @DisplayName("OpenAI API 호출 시 SummaryResponse를 정상 반환한다")
    void summarize_success() {
        // given
        OpenAiResponse response = new OpenAiResponse(
                List.of(new OpenAiResponse.Choice(
                        new OpenAiResponse.Message("assistant", "요약 결과"))
                ),
                "gpt-4o-mini",
                new OpenAiResponse.Usage(10)
        );

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/v1/chat/completions")).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(OpenAiRequest.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(OpenAiResponse.class)).thenReturn(response);

        // when
        SummaryResponse result = openAiClient.summarize("원문 텍스트");

        // then
        assertThat(result.content()).isEqualTo("요약 결과");
        assertThat(result.model()).isEqualTo("gpt-4o-mini");
        assertThat(result.totalTokens()).isEqualTo(10);
    }
}
