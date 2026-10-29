package my.documind.document.exception;

import my.documind.common.exception.BusinessException;
import my.documind.common.exception.ErrorCode;

public class FileEmptyException extends BusinessException {
    public FileEmptyException() {
        super(ErrorCode.FILE_EMPTY);
    }
}