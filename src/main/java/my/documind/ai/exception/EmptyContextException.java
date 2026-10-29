package my.documind.ai.exception;

import lombok.Getter;
import my.documind.common.exception.BusinessException;
import my.documind.common.exception.ErrorCode;

@Getter
public class EmptyContextException extends BusinessException {
    public EmptyContextException() {
        super(ErrorCode.CONTENT_EMPTY);
    }
}
