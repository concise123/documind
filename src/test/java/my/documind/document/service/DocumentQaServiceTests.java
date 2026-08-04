package my.documind.document.service;

import my.documind.ai.service.QaService;
import my.documind.auth.domain.User;
import my.documind.auth.service.UserService;
import my.documind.document.domain.Document;
import my.documind.document.dto.DocumentQaResponse;
import my.documind.document.repository.DocumentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentQaServiceTests {
    @Mock
    private QaService qaService;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private DocumentQaService documentQaService;

    @Test
    @DisplayName("문서 기반 질문에 대한 답변을 제공한다")
    void shouldReturnAnswer_whenDocumentAndQuestionAreProvided() {
        // given
        String email = "test@test.com";
        User user = createUser(email);
        Long documentId = 1L;
        Document document = createDocument(documentId, user);
        String question = "질문";
        String answer = "답변";

        when(userService.getByEmail(email))
                .thenReturn(user);

        when(documentRepository.findByIdAndUser(documentId, user))
                .thenReturn(Optional.of(document));

        when(qaService.ask(any(),eq(question)))
                .thenReturn(answer);

        // when
        DocumentQaResponse documentQaResponse = documentQaService.ask(documentId, email, question);

        // then
        assertThat(documentQaResponse.answer()).isEqualTo(answer);
    }

    private User createUser(String email) {
        return User.builder()
                .email(email)
                .build();
    }

    private Document createDocument(Long documentId, User user) {
        return Document.builder()
                .id(documentId)
                .user(user)
                .extractedText("원문 텍스트")
                .build();
    }
}
