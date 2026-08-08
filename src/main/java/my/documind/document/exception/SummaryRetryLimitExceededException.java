package my.documind.document.exception;

import my.documind.common.exception.ErrorMessage;

public class SummaryRetryLimitExceededException extends RuntimeException {
    public SummaryRetryLimitExceededException() {
        super(ErrorMessage.SUMMARY_RETRY_LIMIT_EXCEEDED.getMessage());
    }
}
