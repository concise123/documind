package my.documind.document.service;

import lombok.RequiredArgsConstructor;
import my.documind.ai.service.QaService;
import my.documind.auth.domain.User;
import my.documind.document.dto.DocumentQaResponse;
import my.documind.auth.service.UserService;
import my.documind.document.domain.Document;
import my.documind.document.exception.DocumentNotFoundException;
import my.documind.document.repository.DocumentRepository;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class DocumentQaService {
    private final QaService qaService;
    private final DocumentRepository documentRepository;
    private final UserService userService;

    public DocumentQaResponse ask(Long documentId, String email, String question) {
        User user = userService.getByEmail(email);
        Document document = documentRepository.findByIdAndUser(documentId, user)
                .orElseThrow(DocumentNotFoundException::new);
        String answer = qaService.ask(document.getExtractedText(), question);
        return new DocumentQaResponse(question, answer);
    }
}
