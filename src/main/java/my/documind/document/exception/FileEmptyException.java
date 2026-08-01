package my.documind.document.exception;

import my.documind.common.exception.ErrorMessage;

public class FileEmptyException extends RuntimeException {
    public FileEmptyException() {
        super(ErrorMessage.FILE_EMPTY.getMessage());
    }
}