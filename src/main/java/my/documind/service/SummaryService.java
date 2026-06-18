package my.documind.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import my.documind.client.OpenAiClient;
import my.documind.domain.Document;
import my.documind.dto.SummaryResponse;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Log4j2
public class SummaryService {
    private static final int MAX_INPUT_LENGTH = 5000;
    private final OpenAiClient openAiClient;

    public SummaryResponse generateSummary(Document document) {
        Long documentId = document.getId();
        log.debug("AI 요약 요청. documentId={}", documentId);
        SummaryResponse response = openAiClient.summarize(trimToLimit(document.getExtractedText()));
        log.debug("AI 요약 응답 수신. documentId={}", documentId);
        return response;
    }

    /**
     * OpenAI 입력 토큰 초과를 방지하기 위해 텍스트를 최대 길이까지만 사용한다.
     */
    private String trimToLimit(String text) {
        return text.length() > MAX_INPUT_LENGTH ? text.substring(0, MAX_INPUT_LENGTH) : text;
    }
}
