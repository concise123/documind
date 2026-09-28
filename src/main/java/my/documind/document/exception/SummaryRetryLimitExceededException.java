package my.documind.document.exception;

import my.documind.common.exception.BusinessException;
import my.documind.common.exception.ErrorCode;

public class SummaryRetryLimitExceededException extends BusinessException {
    public SummaryRetryLimitExceededException() {
        super(ErrorCode.SUMMARY_RETRY_LIMIT_EXCEEDED);
    }
}
