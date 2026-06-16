package my.documind.service;

import my.documind.common.exception.DocumentNotFoundException;
import my.documind.common.exception.ErrorMessage;
import my.documind.common.exception.FileEmptyException;
import my.documind.common.exception.InvalidFileException;
import my.documind.domain.Document;
import my.documind.domain.DocumentStatus;
import my.documind.domain.User;
import my.documind.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTests {
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private PdfTextExtractor pdfExtractor;

    @Mock
    private UserService userService;

    @InjectMocks
    private DocumentService documentService;

    private String email;

    private User user;

    private MultipartFile file;

    @BeforeEach
    void setUp() {
        email = "test@test.com";
        user = createUser();
        file = mock(MultipartFile.class);
        when(userService.getByEmail(email)).thenReturn(user);
    }

    private User createUser() {
        return User.builder()
                .id(1L)
                .email(email)
                .build();
    }

    @Test
    @DisplayName("PDF 문서를 업로드하고 저장한다")
    void upload_success() throws Exception {
        // given
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test.pdf");
        when(file.getContentType()).thenReturn("application/pdf");
        when(documentRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        documentService.upload(List.of(file), email);

        // then
        verify(pdfExtractor).extractText(file.getBytes());
        verify(fileStorageService).store(file);
        verify(documentRepository).saveAll(anyList());
        verify(eventPublisher).publishEvent(any(DocumentUploadedEvent.class));
    }

    @Test
    @DisplayName("업로드 성공 시 상태가 DocumentStatus.UPLOADED가 된다")
    void upload_status_success() {
        // given
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test.pdf");
        when(file.getContentType()).thenReturn("application/pdf");

        // when
        documentService.upload(List.of(file), email);

        // then
        verify(documentRepository).saveAll(
                argThat(documents -> {
                    List<Document> list = new ArrayList<>();
                    documents.forEach(list::add);
                    return list.stream().allMatch(doc -> doc.getStatus() == DocumentStatus.UPLOADED);
                })
        );
    }

    @Test
    @DisplayName("빈 파일 업로드 실패")
    void upload_empty_file() throws Exception {
        // given
        when(file.isEmpty()).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> documentService.upload(List.of(file), user.getEmail()))
                .isInstanceOf(FileEmptyException.class)
                .hasMessage(ErrorMessage.FILE_EMPTY.getMessage());
    }

    @Test
    @DisplayName("txt 파일 업로드 실패")
    void upload_txt_file() throws Exception {
        // given
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test.txt");

        // when & then
        assertThatThrownBy(() -> documentService.upload(List.of(file), user.getEmail()))
                .isInstanceOf(InvalidFileException.class)
                .hasMessage(ErrorMessage.INVALID_FILE_TYPE.getMessage());
    }

    @Test
    @DisplayName("문서가 없을 때 삭제 실패")
    void delete_not_found_file() {
        // given
        when(documentRepository.findByIdAndUser(1L, user)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->documentService.delete(1L, user.getEmail()))
                .isInstanceOf(DocumentNotFoundException.class)
                .hasMessage(ErrorMessage.DOCUMENT_NOT_FOUND.getMessage());
    }
}