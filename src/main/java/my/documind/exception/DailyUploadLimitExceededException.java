package my.documind.exception;

import lombok.Getter;

@Getter
public class DailyUploadLimitExceededException extends RuntimeException {
    public DailyUploadLimitExceededException() {
        super(ErrorMessage.DAILY_UPLOAD_LIMIT_EXCEEDED.getMessage());
    }
}