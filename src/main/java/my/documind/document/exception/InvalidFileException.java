package my.documind.document.exception;

import my.documind.common.exception.ErrorMessage;

public class InvalidFileException extends RuntimeException {
    public InvalidFileException() {
        super(ErrorMessage.INVALID_FILE_TYPE.getMessage());
    }
}