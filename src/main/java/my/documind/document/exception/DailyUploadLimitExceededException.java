package my.documind.document.exception;

import lombok.Getter;
import my.documind.common.exception.ErrorMessage;

@Getter
public class DailyUploadLimitExceededException extends RuntimeException {
    public DailyUploadLimitExceededException() {
        super(ErrorMessage.DAILY_UPLOAD_LIMIT_EXCEEDED.getMessage());
    }
}