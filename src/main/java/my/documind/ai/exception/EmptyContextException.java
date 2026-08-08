package my.documind.ai.exception;

import lombok.Getter;
import my.documind.common.exception.ErrorMessage;

@Getter
public class EmptyContextException extends RuntimeException {
    public EmptyContextException() {
        super(ErrorMessage.CONTENT_EMPTY.getMessage());
    }
}
