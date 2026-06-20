package my.documind.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import my.documind.exception.SummaryException;
import my.documind.domain.Document;
import my.documind.domain.DocumentAiResult;
import my.documind.dto.SummaryResponse;
import my.documind.repository.DocumentRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
@Log4j2
public class AsyncSummaryService {
    private final DocumentRepository documentRepository;
    private final SummaryService summaryService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional
    public void generateSummaryAsync(DocumentUploadedEvent event) {
        Long documentId = event.documentId();
        Document document = documentRepository.findById(documentId).orElseThrow(SummaryException::new);
        String content = document.getExtractedText();
        if (content == null || content.isBlank()) {
            document.fail();
            return;
        }
        log.info("AI 요약 생성 시작. documentId={}", documentId);
        document.startProcessing();
        try {
            // AI 요약 위임
            SummaryResponse response = summaryService.generateSummary(document);
            log.info("AI 요약 생성 완료. documentId={}", documentId);
            // AI 결과 저장
            DocumentAiResult aiResult = DocumentAiResult.summary(response);
            document.addAiResult(aiResult);
            document.complete();
        } catch (Exception e) {
            document.fail();
            log.error("AI 요약 생성 실패. documentId={}", documentId, e);
        }
    }
}
