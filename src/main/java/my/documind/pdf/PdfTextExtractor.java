package my.documind.pdf;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import my.documind.common.exception.ErrorMessage;
import my.documind.pdf.exception.PdfExtractionException;
import my.documind.storage.FileStorage;
import my.documind.storage.UploadFile;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Log4j2
@Component
public class PdfTextExtractor {
    private final FileStorage fileStorage;

    public PdfExtractionResult extractText(UploadFile uploadFile) {
        long start = System.nanoTime();
        Path path = fileStorage.getPath(uploadFile.storedFilename());
        String originalFilename = uploadFile.file().getOriginalFilename();
        try (RandomAccessRead rar = new RandomAccessReadBufferedFile(path.toFile());
             PDDocument document = Loader.loadPDF(rar)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            return new PdfExtractionResult(uploadFile, text);
        } catch (IOException e) {
            throw new PdfExtractionException(ErrorMessage.PDF_TEXT_EXTRACTION_FAILED, e);
        } finally {
            long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            log.info("PDF 추출 시간. file={}, duration={}ms", originalFilename, duration);
        }
    }
}