package my.documind.exception;

import lombok.Getter;

@Getter
public class FileException extends RuntimeException {
    private final ErrorMessage errorMessage;
    public FileException(ErrorMessage errorMessage, Throwable cause) {
        super(errorMessage.getMessage(), cause);
        this.errorMessage = errorMessage;
    }
}