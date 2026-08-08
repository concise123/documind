package my.documind.ai.service;

import lombok.RequiredArgsConstructor;
import my.documind.ai.client.OpenAiClient;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class QaService {
    private final ContextBuilder contextBuilder;
    private final OpenAiClient openAiClient;

    public String ask(String content, String question) {
        String context = contextBuilder.build(content);
        return openAiClient.ask(context, question);
    }
}
