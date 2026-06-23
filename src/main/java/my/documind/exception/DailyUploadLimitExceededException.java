package my.documind.exception;

import lombok.Getter;

@Getter
public class DailyUploadLimitExceededException extends RuntimeException {

    private final ErrorMessage errorMessage;
    private final Object[] args;

    public DailyUploadLimitExceededException(ErrorMessage errorMessage, Object... args) {
        super(errorMessage.format(args));
        this.errorMessage = errorMessage;
        this.args = args;
    }
}