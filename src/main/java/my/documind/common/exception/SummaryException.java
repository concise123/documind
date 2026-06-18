package my.documind.common.exception;

import lombok.Getter;

@Getter
public class SummaryException extends RuntimeException {
    public SummaryException() {
        super(ErrorMessage.SUMMARY_TEXT_EMPTY.getMessage());
    }
}
