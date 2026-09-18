package my.documind.document.service;

import my.documind.ai.service.QaService;
import my.documind.auth.domain.User;
import my.documind.auth.service.UserService;
import my.documind.common.exception.ErrorMessage;
import my.documind.document.dto.DocumentQaResponse;
import my.documind.document.dto.VectorSearchResult;
import my.documind.document.exception.DocumentNotFoundException;
import my.documind.document.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentQaServiceTests {
    @Mock
    private QaService qaService;

    @Mock
    private VectorSearchService vectorSearchService;

    @Mock
    private RetrievalLogger retrievalLogger;

    @Mock
    private UserService userService;

    @Mock
    private DocumentRepository documentRepository;

    @InjectMocks
    private DocumentQaService documentQaService;

    private Long documentId;

    private String email;

    private User user;

    @BeforeEach
    void setUp() {
        documentId = 1L;
        email = "test@test.com";
        user = createUser();

        when(userService.getByEmail(email))
                .thenReturn(user);
    }

    private User createUser() {
        return User.builder()
                .id(1L)
                .email(email)
                .build();
    }

    @Test
    @DisplayName("문서 기반 질문에 대한 답변을 제공한다")
    void shouldReturnAnswer_whenDocumentAndQuestionAreProvided() {
        // given
        String question = "질문";
        String answer = "답변";
        List<VectorSearchResult> searchResults = List.of(
                new VectorSearchResult(1L, "청크 1", 0, 0.12),
                new VectorSearchResult(2L, "청크 2", 1, 0.34));

        when(documentRepository.existsByIdAndUser(documentId, user))
                .thenReturn(true);

        when(vectorSearchService.search(documentId, question))
                .thenReturn(searchResults);

        when(qaService.ask(any(), eq(question)))
                .thenReturn(answer);

        // when
        DocumentQaResponse documentQaResponse = documentQaService.ask(documentId, email, question);

        // then
        assertThat(documentQaResponse.answer()).isEqualTo(answer);
        verify(vectorSearchService).search(documentId, question);
        verify(qaService).ask("청크 1\n\n청크 2", question);
    }

    @Test
    @DisplayName("문서에 접근 권한이 없는 사용자는 질문할 수 없다")
    void shouldRejectQuestion_whenUserDoesNotHaveDocumentAccess() {
        // given
        String question = "질문";

        when(documentRepository.existsByIdAndUser(documentId, user))
                .thenReturn(false);

        // when & then
        assertThatThrownBy(() -> documentQaService.ask(documentId, email, question))
                .isInstanceOf(DocumentNotFoundException.class)
                .hasMessage(ErrorMessage.DOCUMENT_NOT_FOUND.getMessage());
        verify(documentRepository).existsByIdAndUser(documentId, user);
    }
}
