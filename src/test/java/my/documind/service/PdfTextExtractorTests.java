package my.documind.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class PdfTextExtractorTests {
    private PdfTextExtractor pdfTextExtractor;

    @BeforeEach
    void setUp() {
        pdfTextExtractor = new PdfTextExtractor();
    }

    @Test
    @DisplayName("pdf 텍스트 추출")
    void extractPdfText() throws Exception {
        byte[] pdfBytes = Files.readAllBytes(Path.of("src/test/resources/test.pdf"));
        String text = pdfTextExtractor.extractText(pdfBytes);
        assertThat(text).contains("대한");
    }
}