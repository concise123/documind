package my.documind.exception;

public class SummaryRetryLimitExceededException extends RuntimeException {
    public SummaryRetryLimitExceededException() {
        super(ErrorMessage.SUMMARY_RETRY_LIMIT_EXCEEDED.getMessage());
    }
}
