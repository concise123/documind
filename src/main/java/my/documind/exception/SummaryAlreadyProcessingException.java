package my.documind.exception;

public class SummaryAlreadyProcessingException extends RuntimeException {
    public SummaryAlreadyProcessingException() {
        super(ErrorMessage.SUMMARY_ALREADY_PROCESSING.getMessage());
    }
}
