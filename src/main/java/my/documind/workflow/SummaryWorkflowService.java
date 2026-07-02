package my.documind.workflow;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import my.documind.exception.*;
import my.documind.domain.Document;
import my.documind.domain.DocumentAiResult;
import my.documind.dto.SummaryResponse;
import my.documind.repository.DocumentRepository;
import my.documind.service.SummaryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.Semaphore;

@Service
@RequiredArgsConstructor
@Log4j2
public class SummaryWorkflowService {
    private final DocumentRepository documentRepository;
    private final SummaryService summaryService;
    private static final Semaphore OPENAI_SEMAPHORE = new Semaphore(3);

    @Value("${document.summary.max-retry-count}")
    private int maxRetryCount;

    @Transactional
    public void processSummary(Long documentId, SummaryTriggerType type) {
        Document document = find(documentId);
        validateNotProcessing(document);
        validateDocument(document);
        if (type == SummaryTriggerType.RETRY) {
            validateRetry(document);
            document.retryProcessing();
        } else {
            document.startProcessing();
        }
        log.info("AI 요약 생성 시작. documentId={}, retryCount={}", documentId, document.getRetryCount());
        boolean acquired = false;
        try {
            // 동시 실행 개수 제한 (과도한 리소스 사용 방지)
            acquired = OPENAI_SEMAPHORE.tryAcquire();
            if (!acquired) {
                throw new OpenAiConcurrencyLimitException();
            }
            // AI 요약 위임
            SummaryResponse response = summaryService.generateSummary(document);
            log.info("AI 요약 생성 완료. documentId={}", documentId);
            // AI 결과 저장
            DocumentAiResult aiResult = DocumentAiResult.summary(response);
            document.addAiResult(aiResult);
            document.complete();
        } catch (Exception e) {
            log.error("AI 요약 생성 실패. documentId={}", documentId, e);
            document.fail();
        } finally {
            OPENAI_SEMAPHORE.release();
        }
    }

    private Document find(Long id) {
        return documentRepository.findById(id).orElseThrow(DocumentNotFoundException::new);
    }

    private void validateNotProcessing(Document document) {
        if (document.isProcessing()) {
            throw new SummaryAlreadyProcessingException();
        }
    }

    private void validateDocument(Document document) {
        String content = document.getExtractedText();
        if (content == null || content.isBlank()) {
            document.fail();
            throw new SummaryException();
        }
    }

    private void validateRetry(Document document) {
        if (document.getRetryCount() >= maxRetryCount) {
            throw new SummaryRetryLimitExceededException();
        }
    }
}
