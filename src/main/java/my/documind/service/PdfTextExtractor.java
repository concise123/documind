package my.documind.service;

import lombok.extern.log4j.Log4j2;
import my.documind.exception.ErrorMessage;
import my.documind.exception.FileException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

@Log4j2
@Service
public class PdfTextExtractor {
    public String extractText(Path path) {
        try (RandomAccessRead rar = new RandomAccessReadBufferedFile(path.toFile());
             PDDocument document = Loader.loadPDF(rar)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            log.debug("PDF 추출 완료. length={}", text.length());
            return text;
        } catch (IOException e) {
            throw new FileException(ErrorMessage.PDF_TEXT_EXTRACTION_FAILED, e);
        }
    }
}