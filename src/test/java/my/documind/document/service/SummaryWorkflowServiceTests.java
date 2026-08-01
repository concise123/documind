package my.documind.document.service;

import my.documind.document.domain.Document;
import my.documind.document.domain.DocumentStatus;
import my.documind.ai.dto.SummaryResponse;
import my.documind.document.repository.DocumentRepository;
import my.documind.ai.service.SummaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SummaryWorkflowServiceTests {
    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private SummaryService summaryService;

    @InjectMocks
    private SummaryWorkflowService summaryWorkflowService;

    private Long documentId;

    private Document document;

    private SummaryResponse summaryResponse;

    @BeforeEach
    void setUp() {
        documentId = 1L;
        document = spy(createDocument(documentId));

        when(documentRepository.findById(documentId))
                .thenReturn(Optional.of(document));

        summaryResponse = createSummaryResponse();
        ReflectionTestUtils.setField(summaryWorkflowService, "maxRetryCount", 3);
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
    @DisplayName("요약 생성을 처리한다.")
    void shouldProcessSummary_whenTriggerTypeIsValid() {
        // given
        when(summaryService.generateSummary(document.getExtractedText()))
                .thenReturn(summaryResponse);

        // when
        summaryWorkflowService.processSummary(documentId, SummaryTriggerType.START);

        // then
        verify(document).complete();
        verify(document, never()).fail();
        verify(summaryService).generateSummary(document.getExtractedText());
        assertThat(document.getStatus()).isEqualTo(DocumentStatus.COMPLETED);
    }

    @Test
    @DisplayName("요약 생성을 시작한다.")
    void shouldStartSummaryProcessing_whenTriggerTypeIsStart() {
        // given
        when(summaryService.generateSummary(document.getExtractedText()))
                .thenReturn(summaryResponse);

        // when
        summaryWorkflowService.processSummary(documentId, SummaryTriggerType.START);

        // then
        verify(document).startProcessing();
        verify(document, never()).retryProcessing();
    }

    @Test
    @DisplayName("요약 생성을 재시도한다.")
    void shouldRetrySummaryProcessing_whenTriggerTypeIsRetry() {
        // given
        when(summaryService.generateSummary(document.getExtractedText()))
                .thenReturn(summaryResponse);

        // when
        summaryWorkflowService.processSummary(documentId, SummaryTriggerType.RETRY);

        // then
        verify(document).retryProcessing();
        verify(document, never()).startProcessing();
    }

    @Test
    @DisplayName("요약 생성에 실패해도 서비스는 계속 동작한다")
    void shouldNotThrowException_whenExceptionOccurs() {
        // given
        doThrow(new RuntimeException("OpenAI Error"))
                .when(summaryService).generateSummary(any());

        // when & then
        assertDoesNotThrow(() -> summaryWorkflowService.processSummary(documentId, SummaryTriggerType.START));
    }

    @Test
    @DisplayName("요약 생성 실패 시 상태를 실패로 변경한다")
    void shouldSetStatusToFailed_whenSummaryGenerationFails() {
        // when
        when(summaryService.generateSummary(document.getExtractedText()))
                .thenThrow(new RuntimeException("AI 실패"));

        summaryWorkflowService.processSummary(documentId, SummaryTriggerType.START);

        // then
        assertThat(document.getStatus()).isEqualTo(DocumentStatus.FAILED);
    }

    @Test
    @DisplayName("타임아웃 발생 시 상태를 실패로 변경한다")
    void shouldSetStatusToFailed_whenTimeoutExceptionOccurs() {
        // when
        when(summaryService.generateSummary(document.getExtractedText()))
                .thenThrow(new RuntimeException("timeout"));

        summaryWorkflowService.processSummary(documentId, SummaryTriggerType.START);

        // then
        assertThat(document.getStatus()).isEqualTo(DocumentStatus.FAILED);
    }
}
