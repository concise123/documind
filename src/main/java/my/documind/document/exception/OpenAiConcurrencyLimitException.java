package my.documind.document.exception;

import my.documind.common.exception.ErrorMessage;

public class OpenAiConcurrencyLimitException extends RuntimeException {
    public OpenAiConcurrencyLimitException() {
        super(ErrorMessage.OPEN_AI_CONCURRENCY_LIMIT.getMessage());
    }
}
