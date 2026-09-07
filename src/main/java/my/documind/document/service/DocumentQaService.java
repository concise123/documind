package my.documind.document.service;

import lombok.RequiredArgsConstructor;
import my.documind.ai.service.QaService;
import my.documind.document.dto.DocumentQaResponse;
import my.documind.document.dto.VectorSearchResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class DocumentQaService {
    private final QaService qaService;
    private final VectorSearchService vectorSearchService;
    private final RetrievalLogger retrievalLogger;

    public DocumentQaResponse ask(Long documentId, String question) {
        List<VectorSearchResult> searchResults = vectorSearchService.search(documentId, question);
        retrievalLogger.log(question, searchResults);
        String content = searchResults.stream()
                        .map(VectorSearchResult::content)
                        .collect(Collectors.joining("\n\n"));
        String answer = qaService.ask(content, question);
        return new DocumentQaResponse(question, answer);
    }
}
