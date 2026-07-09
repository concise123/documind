package my.documind.event.listener;

import lombok.RequiredArgsConstructor;
import my.documind.event.DocumentUploadedEvent;
import my.documind.workflow.SummaryTriggerType;
import my.documind.workflow.SummaryWorkflowService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SummaryEventListener {
    private final SummaryWorkflowService workflowService;

    @Async("openAiExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(DocumentUploadedEvent event) {
        workflowService.processSummary(event.documentId(), SummaryTriggerType.START);
    }
}
