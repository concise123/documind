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
    @DisplayName("PDF 파일에서 텍스트를 추출한다")
    void shouldExtractText_whenFileIsValid() throws Exception {
        // given
        byte[] pdfBytes = Files.readAllBytes(Path.of("src/test/resources/test.pdf"));

        // when
        String text = pdfTextExtractor.extractText(pdfBytes);

        // then
        assertThat(text).contains("대한");
    }
}