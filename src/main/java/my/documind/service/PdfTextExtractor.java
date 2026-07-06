package my.documind.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import my.documind.config.MemoryLogger;
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

@RequiredArgsConstructor
@Log4j2
@Service
public class PdfTextExtractor {
    private final FileStorageService fileStorageService;
    private final MemoryLogger memoryLogger;

    public PdfExtractionResult extractText(UploadFile uploadFile) {
        Path path = fileStorageService.getPath(uploadFile.storedFilename());
        try (RandomAccessRead rar = new RandomAccessReadBufferedFile(path.toFile());
             PDDocument document = Loader.loadPDF(rar)) {
            String originalFilename = uploadFile.file().getOriginalFilename();
            PDFTextStripper stripper = new PDFTextStripper();
            memoryLogger.logMemory("PDF 추출 시작. file=" + originalFilename);
            String text = stripper.getText(document);
            memoryLogger.logMemory("PDF 추출 완료. file=" + originalFilename + ", length=" + text.length());
            return new PdfExtractionResult(uploadFile, text);
        } catch (IOException e) {
            throw new FileException(ErrorMessage.PDF_TEXT_EXTRACTION_FAILED, e);
        }
    }
}