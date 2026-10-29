package my.documind.ai.service;

import lombok.RequiredArgsConstructor;
import my.documind.ai.client.OpenAiClient;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class QaService {
    private final OpenAiClient openAiClient;

    public String ask(String content, String question) {
        return openAiClient.ask(content, question);
    }
}
