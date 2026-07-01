package my.documind.event.listener;

import my.documind.event.DocumentUploadedEvent;
import my.documind.workflow.SummaryWorkflowService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class SummaryEventListenerTests {
    @Mock
    private SummaryWorkflowService workflowService;

    @InjectMocks
    private SummaryEventListener summaryEventListener;

    @Test
    @DisplayName("문서 업로드 이벤트 수신 시 요약 워크플로우를 실행한다.")
    void shouldStartSummaryWorkflow_whenEventIsReceived() {
        // given
        Long documentId = 1L;
        DocumentUploadedEvent event = new DocumentUploadedEvent(documentId);

        // when
        summaryEventListener.handle(event);

        // then
        verify(workflowService).processSummary(documentId);
    }
}
