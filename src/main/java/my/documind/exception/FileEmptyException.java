package my.documind.exception;

public class FileEmptyException extends RuntimeException {
    public FileEmptyException() {
        super(ErrorMessage.FILE_EMPTY.getMessage());
    }
}