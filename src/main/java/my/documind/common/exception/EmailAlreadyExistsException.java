package my.documind.common.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException() {
        super(ErrorMessage.EMAIL_ALREADY_EXISTS.getMessage());
    }
}