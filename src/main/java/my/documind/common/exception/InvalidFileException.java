package my.documind.common.exception;

public class InvalidFileException extends RuntimeException {
    public InvalidFileException() {
        super(ErrorMessage.INVALID_FILE_TYPE.getMessage());
    }
}