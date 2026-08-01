package my.documind.auth.exception;

import my.documind.common.exception.ErrorMessage;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException() {
        super(ErrorMessage.EMAIL_ALREADY_EXISTS.getMessage());
    }
}