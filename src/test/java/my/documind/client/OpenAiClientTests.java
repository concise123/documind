package my.documind.client;

import my.documind.dto.OpenAiRequest;
import my.documind.dto.OpenAiResponse;
import my.documind.dto.SummaryResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
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
    @DisplayName("요약 요청 시 요약 결과를 반환한다")
    void shouldReturnSummaryResponse_whenRequestSucceeds() {
        // given
        OpenAiResponse response = new OpenAiResponse(
                List.of(new OpenAiResponse.Choice(
                        new OpenAiResponse.Message("assistant", "요약 결과"))
                ),
                "gpt-4o-mini",
                new OpenAiResponse.Usage(10)
        );

        when(restClient.post())
                .thenReturn(requestBodyUriSpec);

        when(requestBodyUriSpec.uri("/v1/chat/completions"))
                .thenReturn(requestBodySpec);

        when(requestBodySpec.body(any(OpenAiRequest.class)))
                .thenReturn(requestBodySpec);

        when(requestBodySpec.retrieve())
                .thenReturn(responseSpec);

        when(responseSpec.body(OpenAiResponse.class))
                .thenReturn(response);

        // when
        SummaryResponse result = openAiClient.summarize("원문 텍스트");

        // then
        assertThat(result.content()).isEqualTo("요약 결과");
        assertThat(result.model()).isEqualTo("gpt-4o-mini");
        assertThat(result.totalTokens()).isEqualTo(10);
    }
}
