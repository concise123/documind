package my.documind.storage.exception;

import lombok.Getter;
import my.documind.common.exception.ErrorMessage;

@Getter
public class FileStorageException extends RuntimeException {
    private final ErrorMessage errorMessage;
    public FileStorageException(ErrorMessage errorMessage, Throwable cause) {
        super(errorMessage.getMessage(), cause);
        this.errorMessage = errorMessage;
    }
}