package my.documind.common.exception;

public class DocumentNotFoundException extends RuntimeException {
    public DocumentNotFoundException() {
        super(ErrorMessage.DOCUMENT_NOT_FOUND.getMessage());
    }
}