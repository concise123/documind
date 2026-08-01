package my.documind.document.listener;

import my.documind.document.event.DocumentUploadedEvent;
import my.documind.document.service.SummaryTriggerType;
import my.documind.document.service.SummaryWorkflowService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class DocumentUploadedEventListenerTests {
    @Mock
    private SummaryWorkflowService workflowService;

    @InjectMocks
    private DocumentUploadedEventListener documentUploadedEventListener;

    @Test
    @DisplayName("문서 업로드 이벤트 수신 시 요약 워크플로우를 실행한다.")
    void shouldStartSummaryWorkflow_whenEventIsReceived() {
        // given
        Long documentId = 1L;
        DocumentUploadedEvent event = new DocumentUploadedEvent(documentId);

        // when
        documentUploadedEventListener.handle(event);

        // then
        verify(workflowService).processSummary(documentId, SummaryTriggerType.START);
    }
}
