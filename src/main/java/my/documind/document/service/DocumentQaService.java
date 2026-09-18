package my.documind.document.service;

import lombok.RequiredArgsConstructor;
import my.documind.ai.service.QaService;
import my.documind.auth.domain.User;
import my.documind.auth.service.UserService;
import my.documind.document.dto.DocumentQaResponse;
import my.documind.document.dto.VectorSearchResult;
import my.documind.document.exception.DocumentNotFoundException;
import my.documind.document.repository.DocumentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class DocumentQaService {
    private final QaService qaService;
    private final VectorSearchService vectorSearchService;
    private final RetrievalLogger retrievalLogger;
    private final UserService userService;
    private final DocumentRepository documentRepository;

    public DocumentQaResponse ask(Long documentId, String email, String question) {
        validateDocumentAccess(documentId, email);
        List<VectorSearchResult> searchResults = vectorSearchService.search(documentId, question);
        retrievalLogger.log(question, searchResults);
        String content = searchResults.stream()
                        .map(VectorSearchResult::content)
                        .collect(Collectors.joining("\n\n"));
        String answer = qaService.ask(content, question);
        return new DocumentQaResponse(question, answer);
    }

    private void validateDocumentAccess(Long documentId, String email) {
        User user = userService.getByEmail(email);
        if (!documentRepository.existsByIdAndUser(documentId, user)) {
            throw new DocumentNotFoundException();
        }
    }
}
