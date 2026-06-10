package my.documind.service;

import my.documind.common.exception.ErrorMessage;
import my.documind.common.exception.FileStorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LocalFileStorageServiceTests {
    @Mock
    private MultipartFile file;

    @InjectMocks
    private LocalFileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        fileStorageService = new LocalFileStorageService();

        ReflectionTestUtils.setField(fileStorageService, "uploadDir", "uploads");
    }

    @Test
    @DisplayName("파일 저장 성공")
    void store_success() throws Exception {
        when(file.getOriginalFilename()).thenReturn("test.pdf");
        String storedFilename = fileStorageService.store(file);
        assertThat(storedFilename).endsWith("_test.pdf");
        verify(file).transferTo(any(Path.class));
    }

    @Test
    @DisplayName("파일 저장 실패")
    void store_failure() throws IOException {
        doThrow(new IOException()).when(file).transferTo(any(Path.class));

        assertThatThrownBy(() -> fileStorageService.store(file))
                .isInstanceOf(FileStorageException.class)
                .hasMessage(ErrorMessage.FILE_SAVE_FAILED.getMessage());
    }
}