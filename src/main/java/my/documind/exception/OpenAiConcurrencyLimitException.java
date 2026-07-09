package my.documind.exception;

public class OpenAiConcurrencyLimitException extends RuntimeException {
    public OpenAiConcurrencyLimitException() {
        super(ErrorMessage.OPEN_AI_CONCURRENCY_LIMIT.getMessage());
    }
}
