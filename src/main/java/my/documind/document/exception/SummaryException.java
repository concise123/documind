package my.documind.document.exception;

import lombok.Getter;
import my.documind.common.exception.ErrorMessage;

@Getter
public class SummaryException extends RuntimeException {
    public SummaryException() {
        super(ErrorMessage.SUMMARY_TEXT_EMPTY.getMessage());
    }
}
