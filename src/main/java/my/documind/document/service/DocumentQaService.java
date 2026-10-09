package my.documind.document.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import my.documind.ai.service.QaService;
import my.documind.auth.domain.User;
import my.documind.auth.service.UserService;
import my.documind.document.dto.DocumentQaResponse;
import my.documind.document.dto.VectorSearchResult;
import my.documind.document.exception.DocumentNotFoundException;
import my.documind.document.exception.QaRateLimitExceededException;
import my.documind.document.repository.DocumentRepository;
import my.documind.document.redis.QaRateLimiter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Log4j2
@RequiredArgsConstructor
@Service
public class DocumentQaService {
    private final QaService qaService;
    private final VectorSearchService vectorSearchService;
    private final RetrievalLogger retrievalLogger;
    private final UserService userService;
    private final DocumentRepository documentRepository;
    private final QaRateLimiter qaRateLimiter;

    public DocumentQaResponse ask(Long documentId, String email, String question) {
        validateDocumentAccess(documentId, email);
        if (!qaRateLimiter.isAllowed(email)) {
            throw new QaRateLimitExceededException();
        }
        long totalStart = System.nanoTime();
        String status = "FAILED";
        try {
            List<VectorSearchResult> searchResults = vectorSearchService.search(documentId, question);
            retrievalLogger.log(question, searchResults);
            String content = searchResults.stream()
                    .map(VectorSearchResult::content)
                    .collect(Collectors.joining("\n\n"));
            String answer = qaService.ask(content, question);
            status = "SUCCESS";
            return new DocumentQaResponse(question, answer);
        } finally {
            long totalDuration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - totalStart);
            log.info("RAG QA 시간. status={}, totalDuration={}ms", status, totalDuration);
        }
    }

    private void validateDocumentAccess(Long documentId, String email) {
        User user = userService.getByEmail(email);
        if (!documentRepository.existsByIdAndUser(documentId, user)) {
            throw new DocumentNotFoundException();
        }
    }
}
