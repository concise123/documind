package my.documind.document.exception;

import my.documind.common.exception.ErrorMessage;

public class SummaryAlreadyProcessingException extends RuntimeException {
    public SummaryAlreadyProcessingException() {
        super(ErrorMessage.SUMMARY_ALREADY_PROCESSING.getMessage());
    }
}
