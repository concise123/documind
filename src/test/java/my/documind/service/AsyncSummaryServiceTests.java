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
        when(documentRepository.findById(documentId)).thenReturn(Optional.of(document));
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
    @DisplayName("문서 업로드 이벤트 수신 시 SummaryService에 AI 요약을 위임한다")
    void generateSummaryAsync_delegation_test() {
        // when
        asyncSummaryService.generateSummaryAsync(event);

        //then
        verify(summaryService).generateSummary(document);
    }

    @Test
    @DisplayName("요약 성공 시 상태가 DocumentStatus.COMPLETED가 된다")
    void generateSummaryAsync_status_success() {
        // given
        Document document = spy(createDocument(documentId));
        when(documentRepository.findById(documentId)).thenReturn(Optional.of(document));
        when(summaryService.generateSummary(document)).thenReturn(summaryResponse);

        // when
        asyncSummaryService.generateSummaryAsync(event);

        // then
        verify(document).startProcessing();
        verify(document).complete();
        assertThat(document.getStatus()).isEqualTo(DocumentStatus.COMPLETED);
    }

    @Test
    @DisplayName("요약 생성 중 예외가 발생해도 예외를 전파하지 않는다")
    void generateSummaryAsync_fail_when_summary_exception_occurs() {
        // given
        doThrow(new RuntimeException("OpenAI Error")).when(summaryService).generateSummary(any());

        // when & then
        assertDoesNotThrow(() -> asyncSummaryService.generateSummaryAsync(event));
    }

    @Test
    @DisplayName("AI 실패 시 FAILED 상태로 변경된다")
    void generateSummaryAsync_fail_when_ai_fails() {
        // when
        when(summaryService.generateSummary(document)).thenThrow(new RuntimeException("AI 실패"));
        asyncSummaryService.generateSummaryAsync(event);

        // then
        assertThat(document.getStatus()).isEqualTo(DocumentStatus.FAILED);
    }

    @Test
    @DisplayName("타임아웃 예외 발생 시 FAILED 상태로 변경된다")
    void generateSummaryAsync_fail_when_timeout_exception_occurs() {
        // when
        when(summaryService.generateSummary(document)).thenThrow(new RuntimeException("timeout"));
        asyncSummaryService.generateSummaryAsync(event);

        // then
        assertThat(document.getStatus()).isEqualTo(DocumentStatus.FAILED);
    }
}
