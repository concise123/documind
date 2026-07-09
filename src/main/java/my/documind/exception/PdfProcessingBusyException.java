package my.documind.exception;

public class PdfProcessingBusyException extends RuntimeException {
    public PdfProcessingBusyException() {
        super(ErrorMessage.PDF_PROCESSING_BUSY.getMessage());
    }
}
