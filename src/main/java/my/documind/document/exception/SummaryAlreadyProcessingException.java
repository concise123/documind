package my.documind.document.exception;

import my.documind.common.exception.BusinessException;
import my.documind.common.exception.ErrorCode;

public class SummaryAlreadyProcessingException extends BusinessException {
    public SummaryAlreadyProcessingException() {
        super(ErrorCode.SUMMARY_ALREADY_PROCESSING);
    }
}
