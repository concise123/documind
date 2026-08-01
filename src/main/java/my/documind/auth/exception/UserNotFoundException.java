package my.documind.auth.exception;

import my.documind.common.exception.ErrorMessage;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super(ErrorMessage.USER_SESSION_INVALID.getMessage());
    }
}