package my.documind.service;

import my.documind.config.MemoryLogger;
import my.documind.upload.PdfExtractionResult;
import my.documind.upload.UploadFile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PdfTextExtractorTests {
    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private MemoryLogger memoryLogger;

    @InjectMocks
    private PdfTextExtractor pdfTextExtractor;

    @Test
    @DisplayName("PDF 파일에서 텍스트를 추출한다")
    void shouldExtractText_whenFileIsValid() throws Exception {
        // given
        Path path = Path.of("src/test/resources/test.pdf");
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                Files.readAllBytes(path));
        String storedFilename = anyString();
        UploadFile uploadFile = new UploadFile(file, storedFilename);

        when(fileStorageService.getPath(storedFilename))
                .thenReturn(path);

        // when
        PdfExtractionResult result = pdfTextExtractor.extractText(uploadFile);

        // then
        assertThat(result.text()).contains("대한민국");
    }
}
