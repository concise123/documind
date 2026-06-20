package my.documind.service;

import my.documind.domain.Document;
import my.documind.domain.DocumentStatus;
import my.documind.dto.SummaryResponse;
import my.documind.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsyncSummaryServiceTests {
    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private SummaryService summaryService;

    @InjectMocks
    private AsyncSummaryService asyncSummaryService;

    private DocumentUploadedEvent event;

    private Long documentId;

    private Document document;

    private SummaryResponse summaryResponse;

    @BeforeEach
    void setUp() {
        event = new DocumentUploadedEvent(1L);
        documentId = event.documentId();
        document = createDocument(documentId);

        when(documentRepository.findById(documentId))
                .thenReturn(Optional.of(document));

        summaryResponse = createSummaryResponse();
    }

    private Document createDocument(Long documentId) {
        return Document.builder()
                .id(documentId)
                .status(DocumentStatus.UPLOADED)
                .extractedText("원문 텍스트")
                .build();
    }

    private SummaryResponse createSummaryResponse() {
        return new SummaryResponse("result", "gpt", 10);
    }

    @Test
    @DisplayName("문서 업로드 이벤트 수신 시 요약 생성을 시작한다")
    void shouldDelegateToSummaryService_whenEventIsReceived() {
        // when
        asyncSummaryService.generateSummaryAsync(event);

        //then
        verify(summaryService).generateSummary(document);
    }

    @Test
    @DisplayName("요약 생성 완료 시 상태를 완료로 변경한다")
    void shouldSetStatusToCompleted_whenSummaryGenerationSucceeds() {
        // given
        Document document = spy(createDocument(documentId));

        when(documentRepository.findById(documentId))
                .thenReturn(Optional.of(document));

        when(summaryService.generateSummary(document))
                .thenReturn(summaryResponse);

        // when
        asyncSummaryService.generateSummaryAsync(event);

        // then
        verify(document).startProcessing();
        verify(document).complete();
        assertThat(document.getStatus()).isEqualTo(DocumentStatus.COMPLETED);
    }

    @Test
    @DisplayName("요약 생성에 실패해도 서비스는 계속 동작한다")
    void shouldNotThrowException_whenExceptionOccurs() {
        // given
        doThrow(new RuntimeException("OpenAI Error"))
                .when(summaryService).generateSummary(any());

        // when & then
        assertDoesNotThrow(() -> asyncSummaryService.generateSummaryAsync(event));
    }

    @Test
    @DisplayName("요약 생성 실패 시 상태를 실패로 변경한다")
    void shouldSetStatusToFailed_whenSummaryGenerationFails() {
        // when
        when(summaryService.generateSummary(document))
                .thenThrow(new RuntimeException("AI 실패"));

        asyncSummaryService.generateSummaryAsync(event);

        // then
        assertThat(document.getStatus()).isEqualTo(DocumentStatus.FAILED);
    }

    @Test
    @DisplayName("타임아웃 발생 시 상태를 실패로 변경한다")
    void shouldSetStatusToFailed_whenTimeoutExceptionOccurs() {
        // when
        when(summaryService.generateSummary(document))
                .thenThrow(new RuntimeException("timeout"));

        asyncSummaryService.generateSummaryAsync(event);

        // then
        assertThat(document.getStatus()).isEqualTo(DocumentStatus.FAILED);
    }
}
