package my.documind.upload;

public record PdfExtractionResult (UploadFile uploadFile, String text) {
    public PdfExtractionResult withText(String text) {
        return new PdfExtractionResult(uploadFile, text);
    }
}
