package my.documind.document.exception;

import my.documind.common.exception.BusinessException;
import my.documind.common.exception.ErrorCode;

public class OpenAiConcurrencyLimitException extends BusinessException {
    public OpenAiConcurrencyLimitException() {
        super(ErrorCode.OPEN_AI_CONCURRENCY_LIMIT);
    }
}
