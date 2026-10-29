package my.documind.document.exception;

import my.documind.common.exception.BusinessException;
import my.documind.common.exception.ErrorCode;

public class InvalidFileException extends BusinessException {
    public InvalidFileException() {
        super(ErrorCode.INVALID_FILE_TYPE);
    }
}