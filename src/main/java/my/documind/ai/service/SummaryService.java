package my.documind.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import my.documind.ai.client.OpenAiClient;
import my.documind.ai.dto.SummaryResponse;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Log4j2
public class SummaryService {
    private final ContextBuilder contextBuilder;
    private final OpenAiClient openAiClient;

    public SummaryResponse generateSummary(String content) {
        String context = contextBuilder.build(content);
        return openAiClient.summarize(context);
    }
}
