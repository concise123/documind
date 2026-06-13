package my.documind.common.exception;

import lombok.Getter;

@Getter
public class FileStorageException extends RuntimeException {
    private final ErrorMessage errorMessage;
    public FileStorageException(ErrorMessage errorMessage, Throwable cause) {
        super(errorMessage.getMessage(), cause);
        this.errorMessage = errorMessage;
    }
}