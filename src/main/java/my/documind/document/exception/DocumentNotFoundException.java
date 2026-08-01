package my.documind.document.exception;

import my.documind.common.exception.ErrorMessage;

public class DocumentNotFoundException extends RuntimeException {
    public DocumentNotFoundException() {
        super(ErrorMessage.DOCUMENT_NOT_FOUND.getMessage());
    }
}