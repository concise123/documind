package my.documind.document.exception;

import lombok.Getter;
import my.documind.common.exception.BusinessException;
import my.documind.common.exception.ErrorCode;

@Getter
public class DailyUploadLimitExceededException extends BusinessException {
    public DailyUploadLimitExceededException() {
        super(ErrorCode.DAILY_UPLOAD_LIMIT_EXCEEDED);
    }
}