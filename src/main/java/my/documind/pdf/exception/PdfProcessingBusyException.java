package my.documind.pdf.exception;

import my.documind.common.exception.ErrorMessage;

public class PdfProcessingBusyException extends RuntimeException {
    public PdfProcessingBusyException() {
        super(ErrorMessage.PDF_PROCESSING_BUSY.getMessage());
    }
}
