package my.documind.document.service;

import my.documind.auth.service.UserService;
import my.documind.common.exception.ErrorMessage;
import my.documind.document.event.DocumentUploadedEvent;
import my.documind.document.exception.DailyUploadLimitExceededException;
import my.documind.document.exception.DocumentNotFoundException;
import my.documind.document.exception.FileEmptyException;
import my.documind.document.exception.InvalidFileException;
import my.documind.document.domain.Document;
import my.documind.document.domain.DocumentStatus;
import my.documind.auth.domain.User;
import my.documind.document.repository.DocumentRepository;
import my.documind.storage.FileStorage;
import my.documind.pdf.PdfBatchRunner;
import my.documind.pdf.PdfExtractionResult;
import my.documind.storage.UploadFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTests {
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private FileStorage fileStorage;

    @Mock
    private PdfBatchRunner pdfBatchRunner;

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

        when(userService.getByEmail(email))
                .thenReturn(user);

        ReflectionTestUtils.setField(documentService, "dailyUploadLimit", 3);
    }

    private User createUser() {
        return User.builder()
                .id(1L)
                .email(email)
                .build();
    }

    @Test
    @DisplayName("문서 업로드 시 상태를 업로드 완료로 설정한다")
    void shouldSetStatusToUploaded_whenValidDocument() throws Exception {
        // given
        when(file.isEmpty())
                .thenReturn(false);

        when(file.getOriginalFilename())
                .thenReturn("test.pdf");

        when(file.getContentType())
                .thenReturn("application/pdf");

        // when
        documentService.upload(List.of(file), email);

        // then
        verify(pdfBatchRunner).extractAll(anyList());
        verify(fileStorage).store(file);
        verify(documentRepository).saveAll(anyList());
        verify(documentRepository).saveAll(
                argThat(documents -> {
                    List<Document> list = new ArrayList<>();
                    documents.forEach(list::add);
                    return list.stream().allMatch(doc -> doc.getStatus() == DocumentStatus.UPLOADED);
                })
        );
    }

    @Test
    @DisplayName("문서 저장 후 AI 요약 생성을 요청한다")
    void shouldPublishDocumentUploadedEvent_whenDocumentIsSaved() throws IOException {
        // given
        when(file.isEmpty())
                .thenReturn(false);

        when(file.getOriginalFilename())
                .thenReturn("test.pdf");

        when(file.getContentType())
                .thenReturn("application/pdf");

        String storedFilename = "uuid.pdf";
        UploadFile uploadFile = new UploadFile(file, storedFilename);
        PdfExtractionResult result = new PdfExtractionResult(uploadFile, "text");

        when(pdfBatchRunner.extractAll(anyList()))
                .thenReturn(List.of(result));

        when(documentRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        documentService.upload(List.of(file), email);

        // then
        verify(eventPublisher).publishEvent(any(DocumentUploadedEvent.class));
    }

    @Test
    @DisplayName("빈 파일은 업로드할 수 없다")
    void shouldThrowException_whenFileIsEmpty() throws Exception {
        // given
        when(file.isEmpty())
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> documentService.upload(List.of(file), user.getEmail()))
                .isInstanceOf(FileEmptyException.class)
                .hasMessage(ErrorMessage.FILE_EMPTY.getMessage());
    }

    @Test
    @DisplayName("PDF 형식이 아닌 파일은 업로드할 수 없다")
    void shouldThrowException_whenFileIsNotPdf() throws Exception {
        // given
        when(file.isEmpty())
                .thenReturn(false);

        when(file.getOriginalFilename())
                .thenReturn("test.txt");

        // when & then
        assertThatThrownBy(() -> documentService.upload(List.of(file), user.getEmail()))
                .isInstanceOf(InvalidFileException.class)
                .hasMessage(ErrorMessage.INVALID_FILE_TYPE.getMessage());
    }

    @Test
    @DisplayName("존재하지 않는 문서는 삭제할 수 없다")
    void shouldThrowException_whenDocumentDoesNotExist() {
        // given
        when(documentRepository.findByIdAndUser(1L, user))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->documentService.delete(1L, user.getEmail()))
                .isInstanceOf(DocumentNotFoundException.class)
                .hasMessage(ErrorMessage.DOCUMENT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("일일 업로드 제한을 초과하면 업로드를 실패한다")
    void shouldThrowException_whenDailyUploadLimitExceeded() {
        // given
        when(documentRepository.countByUserAndRegDateAfter(eq(user), any(LocalDateTime.class)))
                .thenReturn(3L);

        // when & then
        assertThatThrownBy(() -> documentService.upload(List.of(file), user.getEmail()))
                .isInstanceOf(DailyUploadLimitExceededException.class);
    }

    @Test
    @DisplayName("일일 업로드 제한 이내이면 문서를 정상 업로드한다")
    void shouldUploadSuccessfully_whenWithinDailyUploadLimit() {
        // given
        when(file.isEmpty())
                .thenReturn(false);

        when(file.getOriginalFilename())
                .thenReturn("test.pdf");

        when(file.getContentType())
                .thenReturn("application/pdf");

        when(documentRepository.countByUserAndRegDateAfter(eq(user), any(LocalDateTime.class)))
                .thenReturn(1L);

        // when & then
        assertThatCode(() -> documentService.upload(List.of(file), user.getEmail()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("PDF 추출에 실패하면 파일을 정리한다")
    void shouldCleanupUploadedFiles_whenPdfBatchRunnerFails() {
        // given
        when(file.isEmpty())
                .thenReturn(false);

        when(file.getOriginalFilename())
                .thenReturn("test.pdf");

        when(file.getContentType())
                .thenReturn("application/pdf");

        doThrow(new RuntimeException())
                .when(pdfBatchRunner).extractAll(anyList());

        // when & then
        assertThatThrownBy(() -> documentService.upload(List.of(file), email))
                .isInstanceOf(RuntimeException.class);
        verify(fileStorage).delete(any());
    }
}