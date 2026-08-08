package my.documind.document.listener;

import lombok.RequiredArgsConstructor;
import my.documind.document.event.DocumentUploadedEvent;
import my.documind.document.service.SummaryTriggerType;
import my.documind.document.service.SummaryWorkflowService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class DocumentUploadedEventListener {
    private final SummaryWorkflowService workflowService;

    @Async("openAiExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(DocumentUploadedEvent event) {
        workflowService.processSummary(event.documentId(), SummaryTriggerType.START);
    }
}
