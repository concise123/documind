package my.documind.auth.exception;

import my.documind.common.exception.BusinessException;
import my.documind.common.exception.ErrorCode;

public class EmailAlreadyExistsException extends BusinessException {
    public EmailAlreadyExistsException() {
        super(ErrorCode.EMAIL_ALREADY_EXISTS);
    }
}