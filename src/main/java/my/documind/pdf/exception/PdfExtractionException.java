package my.documind.pdf.exception;

import lombok.Getter;
import my.documind.common.exception.ErrorMessage;

@Getter
public class PdfExtractionException extends RuntimeException {
    private final ErrorMessage errorMessage;
    public PdfExtractionException(ErrorMessage errorMessage, Throwable cause) {
        super(errorMessage.getMessage(), cause);
        this.errorMessage = errorMessage;
    }
}