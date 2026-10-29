package my.documind.pdf;

import my.documind.storage.UploadFile;

public record PdfExtractionResult (UploadFile uploadFile, String text) {
    public PdfExtractionResult withText(String text) {
        return new PdfExtractionResult(uploadFile, text);
    }
}
