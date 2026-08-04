package my.documind.ai.service;

import my.documind.ai.exception.EmptyContextException;
import org.springframework.stereotype.Component;

/**
 * AI 모델에 전달할 Context를 생성한다.
 * 입력 텍스트 정규화 및 문자 수 제한을 담당한다.
 */
@Component
public class ContextBuilder {
    private static final int MAX_CONTEXT_CHARACTERS = 5000;

    public String build(String content) {
        validate(content);
        String normalizedContent = clean(content);
        return truncate(normalizedContent);
    }

    private void validate(String content) {
        if (content == null || content.isBlank()) {
            throw new EmptyContextException();
        }
    }

    private String clean(String content) {
        return content
                .replaceAll("[ \\t]+", " ") // 연속된 공백(space)과 탭(tab)을 공백 하나로 변경함
                .replaceAll("\\n{3,}", "\n\n") // 줄바꿈이 3번 이상 연속되면 2번으로 줄임
                .trim();
    }

    private String truncate(String content) {
        if (content.length() <= MAX_CONTEXT_CHARACTERS) {
            return content;
        }
        return content.substring(0, MAX_CONTEXT_CHARACTERS);
    }
}
