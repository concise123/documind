package my.documind.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import my.documind.exception.ErrorMessage;
import my.documind.exception.FileException;
import my.documind.upload.PdfExtractionResult;
import my.documind.upload.UploadFile;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Log4j2
@Service
public class PdfTextExtractor {
    private final FileStorageService fileStorageService;

    public PdfExtractionResult extractText(UploadFile uploadFile) {
        long start = System.nanoTime();
        Path path = fileStorageService.getPath(uploadFile.storedFilename());
        String originalFilename = uploadFile.file().getOriginalFilename();
        try (RandomAccessRead rar = new RandomAccessReadBufferedFile(path.toFile());
             PDDocument document = Loader.loadPDF(rar)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            return new PdfExtractionResult(uploadFile, text);
        } catch (IOException e) {
            throw new FileException(ErrorMessage.PDF_TEXT_EXTRACTION_FAILED, e);
        } finally {
            long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            log.info("PDF 추출 시간. file={}, duration={}ms", originalFilename, duration);
        }
    }
}