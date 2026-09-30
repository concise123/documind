package my.documind.document.exception;

import my.documind.common.exception.BusinessException;
import my.documind.common.exception.ErrorCode;

public class QaRateLimitExceededException extends BusinessException {
    public QaRateLimitExceededException() {
        super(ErrorCode.QA_RATE_LIMIT_EXCEEDED);
    }
}
