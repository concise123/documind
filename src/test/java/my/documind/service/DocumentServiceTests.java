package my.documind.service;

import my.documind.common.exception.DocumentNotFoundException;
import my.documind.common.exception.ErrorMessage;
import my.documind.common.exception.FileEmptyException;
import my.documind.common.exception.InvalidFileException;
import my.documind.domain.User;
import my.documind.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTests {
    @Mock
    private UserService userService;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private MultipartFile file;

    @InjectMocks
    private DocumentService documentService;

    private User user;

    private String email;

    @BeforeEach
    void setUp() {
        email = "test@test.com";

        user = User.builder()
                .id(1L)
                .email(email)
                .build();
    }

    @Test
    @DisplayName("업로드 성공")
    void upload_success() throws Exception {
        when(userService.getByEmail(email)).thenReturn(user);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test.pdf");
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getSize()).thenReturn(100L);

        when(fileStorageService.store(file)).thenReturn("uuid_test.pdf");

        documentService.upload(List.of(file), email);

        verify(fileStorageService, times(1)).store(file);
        verify(documentRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("빈 파일 업로드 실패")
    void upload_empty_file() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile("file",
                "", "application/pdf", new byte[0]);

        assertThatThrownBy(() -> documentService.upload(List.of(emptyFile), user.getEmail()))
                .isInstanceOf(FileEmptyException.class)
                .hasMessage(ErrorMessage.FILE_EMPTY.getMessage());
    }

    @Test
    @DisplayName("txt 파일 업로드 실패")
    void upload_txt_file() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile("file",
                "test.txt", "text/plain", "test".getBytes());

        assertThatThrownBy(() -> documentService.upload(List.of(file), user.getEmail()))
                .isInstanceOf(InvalidFileException.class)
                .hasMessage(ErrorMessage.INVALID_FILE_TYPE.getMessage());
    }

    @Test
    @DisplayName("문서가 없을 때 삭제 실패")
    void delete_not_found_file() {
        when(userService.getByEmail(email)).thenReturn(user);
        when(documentRepository.findByIdAndUser(1L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->documentService.delete(1L, user.getEmail()))
                .isInstanceOf(DocumentNotFoundException.class)
                .hasMessage(ErrorMessage.DOCUMENT_NOT_FOUND.getMessage());
    }
}