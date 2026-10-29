package my.documind.storage.exception;

import lombok.Getter;
import my.documind.common.exception.BusinessException;
import my.documind.common.exception.ErrorCode;

@Getter
public class FileStorageException extends BusinessException {
    public FileStorageException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}