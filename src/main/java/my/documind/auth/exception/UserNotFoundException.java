package my.documind.auth.exception;

import my.documind.common.exception.BusinessException;
import my.documind.common.exception.ErrorCode;

public class UserNotFoundException extends BusinessException {
    public UserNotFoundException() {
        super(ErrorCode.USER_SESSION_INVALID);
    }
}