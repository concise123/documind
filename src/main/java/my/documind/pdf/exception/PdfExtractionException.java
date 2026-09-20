package my.documind.pdf.exception;

import lombok.Getter;
import my.documind.common.exception.BusinessException;
import my.documind.common.exception.ErrorCode;

@Getter
public class PdfExtractionException extends BusinessException {
    public PdfExtractionException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}