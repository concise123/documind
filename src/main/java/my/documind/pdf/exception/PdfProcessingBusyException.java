package my.documind.pdf.exception;

import my.documind.common.exception.BusinessException;
import my.documind.common.exception.ErrorCode;

public class PdfProcessingBusyException extends BusinessException {
    public PdfProcessingBusyException() {
        super(ErrorCode.PDF_PROCESSING_BUSY);
    }
}
