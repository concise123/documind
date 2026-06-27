package my.documind.service;

import my.documind.exception.ErrorMessage;
import my.documind.exception.FileException;
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
    @DisplayName("파일을 저장한다")
    void shouldStoreFile_whenFileIsValid() throws Exception {
        // given
        when(file.getOriginalFilename())
                .thenReturn("test.pdf");

        // when
        String storedFilename = fileStorageService.store(file);

        // then
        verify(file).transferTo(any(Path.class));
    }

    @Test
    @DisplayName("파일 저장에 실패하면 예외를 발생시킨다")
    void shouldThrowException_whenFileStorageFails() throws IOException {
        // given
        doThrow(new IOException())
                .when(file).transferTo(any(Path.class));

        // when & then
        assertThatThrownBy(() -> fileStorageService.store(file))
                .isInstanceOf(FileException.class)
                .hasMessage(ErrorMessage.FILE_SAVE_FAILED.getMessage());
    }
}