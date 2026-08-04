package my.documind.ai.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import my.documind.ai.dto.OpenAiRequest;
import my.documind.ai.dto.OpenAiResponse;
import my.documind.ai.dto.SummaryResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Log4j2
@RequiredArgsConstructor
@Component
public class OpenAiClient {
    private static final double SUMMARY_TEMPERATURE = 0.3;
    private static final double QA_TEMPERATURE = 0.2;

    private final RestClient restClient;

    @Value("${openai.model}")
    private String openAiModel;

    public SummaryResponse summarize(String context) {
        long start = System.currentTimeMillis();
        try {
            List<OpenAiRequest.Message> messages = List.of(
                    new OpenAiRequest.Message("system", "다음 문서를 3~5줄로 요약해줘."),
                    new OpenAiRequest.Message("user", context));
            OpenAiRequest request = createRequest(messages, SUMMARY_TEMPERATURE);
            OpenAiResponse response = restClient.post()
                    .uri("/v1/chat/completions")
                    .body(request)
                    .retrieve()
                    .body(OpenAiResponse.class);
            return new SummaryResponse(response.getContent(), response.model(), response.usage().totalTokens());
        } finally {
            long duration = System.currentTimeMillis() - start;
            log.info("OpenAI API 호출 시간. duration={}ms", duration);
        }
    }

    public String ask(String context, String question) {
        long start = System.currentTimeMillis();
        try {
            List<OpenAiRequest.Message> messages = List.of(
                    new OpenAiRequest.Message("system", """
                            당신은 문서 기반 질의응답 AI입니다. 반드시 아래 [문서 내용] 안에 있는 정보만 사용해서 답변하세요.

                            [중요 규칙]
                            1. 문서에 없는 내용은 절대 추측하지 마세요.
                            2. 답을 찾을 수 없으면 반드시 "문서에 해당 내용이 없습니다"라고 답하세요.
                            3. 외부 지식은 사용하지 마세요.
                            4. 답변은 간결하고 핵심만 작성하세요.
                            5. 필요하면 bullet point로 정리하세요.
                            
                            [답변 형식]
                            - 핵심 답변:
                            - 근거:"""),
                    new OpenAiRequest.Message("user", """
                            [문서 내용]
                            %s

                            [사용자 질문]
                            %s""".formatted(context, question)));
            OpenAiRequest request = createRequest(messages, QA_TEMPERATURE);
            OpenAiResponse response = restClient.post()
                    .uri("/v1/chat/completions")
                    .body(request)
                    .retrieve()
                    .body(OpenAiResponse.class);
            return response.getContent();
        } finally {
            long duration = System.currentTimeMillis() - start;
            log.info("OpenAI API 호출 시간. duration={}ms", duration);
        }
    }

    private OpenAiRequest createRequest(List<OpenAiRequest.Message> messages, double temperature) {
        return new OpenAiRequest(openAiModel, messages, temperature);
    }
}
