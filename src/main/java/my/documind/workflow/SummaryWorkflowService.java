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

    /**
     * 문서 AI 요약 시 상태 관리와 AI workflow orchestration을 담당한다.
     *
     * <p>
     * 문서 업로드 이후 또는 실패 후 재시도 요청에 따라 START / RETRY 트리거 타입에 맞게 상태를 전이한다.
     * AI 호출은 {@link SummaryService}에 위임하며 AI 요약을 수행한 뒤 결과를 저장한다.
     * </p>
     *
     * <h3>처리 흐름</h3>
     * <ol>
     *     <li>Document 조회</li>
     *     <li>중복 실행 방지 검증
     *     <li>원본 텍스트 검증</li>
     *     <li>START / RETRY 상태 전이</li>
     *     <li>AI 요약 요청 (SummaryService 위임)</li>
     *     <li>성공 시 COMPLETED 처리</li>
     *     <li>실패 시 FAILED 처리</li>
     * </ol>
     *
     * <h3>트리거 타입</h3>
     * <ul>
     *     <li>START: 최초 요약 실행 (UPLOADED → PROCESSING)</li>
     *     <li>RETRY: 실패 이후 재시도 (FAILED → PROCESSING, retryCount 증가)</li>
     * </ul>
     *
     * @param documentId 처리할 문서 ID
     * @param type START 또는 RETRY 트리거 타입
     * @throws DocumentNotFoundException 문서 조회에 실패한 경우
     * @throws SummaryAlreadyProcessingException 이미 처리 중인 경우
     * @throws SummaryException 문서 내용이 없는 경우
     * @throws SummaryRetryLimitExceededException 재시도 횟수를 초과한 경우
     * @throws OpenAiConcurrencyLimitException OpenAI 동시성 제한을 초과한 경우
     */
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
            SummaryResponse response = summaryService.generateSummary(document.getExtractedText());
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
