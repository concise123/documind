package my.documind.service;

import my.documind.config.MemoryLogger;
import my.documind.exception.*;
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
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

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
    private FileStorageService fileStorageService;

    @Mock
    private MemoryLogger memoryLogger;

    @Mock
    private PdfTextExtractor pdfExtractor;

    @Mock
    private UserService userService;

    @Mock
    private ThreadPoolTaskExecutor pdfExecutor;

    @Mock
    private Future<String> future;

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

        when(pdfExtractor.extractText(any()))
                .thenReturn("text");

        when(pdfExecutor.submit(any(Callable.class)))
                .thenAnswer(invocation -> {
                    Callable<String> task = invocation.getArgument(0);
                    FutureTask<String> future = new FutureTask<>(task);
                    future.run();
                    return future;
                });

        // when
        documentService.upload(List.of(file), email);

        // then
        verify(pdfExtractor).extractText(file.getBytes());
        verify(fileStorageService).store(file);
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
    void shouldPublishDocumentUploadedEvent_whenDocumentIsSaved() {
        // given
        when(file.isEmpty())
                .thenReturn(false);

        when(file.getOriginalFilename())
                .thenReturn("test.pdf");

        when(file.getContentType())
                .thenReturn("application/pdf");

        when(pdfExtractor.extractText(any()))
                .thenReturn("text");

        when(pdfExecutor.submit(any(Callable.class)))
                .thenAnswer(invocation -> {
                    Callable<String> task = invocation.getArgument(0);
                    FutureTask<String> future = new FutureTask<>(task);
                    future.run();
                    return future;
                });

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
    @DisplayName("하루 업로드 제한을 초과하면 업로드를 실패한다")
    void shouldThrowException_whenDailyUploadLimitExceeded() {
        // given
        when(documentRepository.countByUserAndRegDateAfter(eq(user), any(LocalDateTime.class)))
                .thenReturn(3L);

        // when & then
        assertThatThrownBy(() -> documentService.upload(List.of(file), user.getEmail()))
                .isInstanceOf(DailyUploadLimitExceededException.class);
    }

    @Test
    @DisplayName("하루 업로드 제한 이내이면 문서를 정상 업로드한다")
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

        when(pdfExtractor.extractText(any()))
                .thenReturn("text");

        when(pdfExecutor.submit(any(Callable.class)))
                .thenAnswer(invocation -> {
                    Callable<String> task = invocation.getArgument(0);
                    FutureTask<String> future = new FutureTask<>(task);
                    future.run();
                    return future;
                });

        // when & then
        assertThatCode(() -> documentService.upload(List.of(file), user.getEmail()))
                .doesNotThrowAnyException();
    }
}