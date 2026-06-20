package my.documind.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super(ErrorMessage.USER_SESSION_INVALID.getMessage());
    }
}