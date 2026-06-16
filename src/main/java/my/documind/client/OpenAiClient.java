package my.documind.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import my.documind.dto.OpenAiRequest;
import my.documind.dto.OpenAiResponse;
import my.documind.dto.SummaryResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Log4j2
@RequiredArgsConstructor
@Component
public class OpenAiClient {
    private final RestClient restClient;;

    public SummaryResponse summarize(String text) {
        OpenAiRequest request = createRequest(text);
        OpenAiResponse response = restClient.post()
                .uri("/v1/chat/completions")
                .body(request)
                .retrieve()
                .body(OpenAiResponse.class);
        return new SummaryResponse(response.getContent(), response.model(), response.usage().totalTokens());
    }

    private OpenAiRequest createRequest(String text) {
        return new OpenAiRequest("gpt-4o-mini",
                List.of(new OpenAiRequest.Message("system", "다음 문서를 3~5줄로 요약해줘."),
                        new OpenAiRequest.Message("user", text)));
    }
}
