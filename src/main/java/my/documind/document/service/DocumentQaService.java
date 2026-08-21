package my.documind.document.service;

import lombok.RequiredArgsConstructor;
import my.documind.ai.service.QaService;
import my.documind.document.domain.DocumentChunk;
import my.documind.document.dto.DocumentQaResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class DocumentQaService {
    private final QaService qaService;
    private final VectorSearchService vectorSearchService;

    public DocumentQaResponse ask(Long documentId, String question) {
        List<DocumentChunk> chunks = vectorSearchService.search(documentId, question);
        String content = chunks.stream()
                        .map(DocumentChunk::getContent)
                        .collect(Collectors.joining("\n\n"));
        String answer = qaService.ask(content, question);
        return new DocumentQaResponse(question, answer);
    }
}
