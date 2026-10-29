package my.documind.document.exception;

import my.documind.common.exception.BusinessException;
import my.documind.common.exception.ErrorCode;

public class DocumentNotFoundException extends BusinessException {
    public DocumentNotFoundException() {
        super(ErrorCode.DOCUMENT_NOT_FOUND);
    }
}