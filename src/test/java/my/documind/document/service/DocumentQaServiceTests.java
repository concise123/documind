package my.documind.document.service;

import my.documind.ai.service.QaService;
import my.documind.document.dto.DocumentQaResponse;
import my.documind.document.dto.VectorSearchResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

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

    @InjectMocks
    private DocumentQaService documentQaService;

    @Test
    @DisplayName("문서 기반 질문에 대한 답변을 제공한다")
    void shouldReturnAnswer_whenDocumentAndQuestionAreProvided() {
        // given
        Long documentId = 1L;
        String question = "질문";
        String answer = "답변";
        List<VectorSearchResult> searchResults = List.of(
                new VectorSearchResult(1L, "청크 1", 0, 0.12),
                new VectorSearchResult(2L, "청크 2", 1, 0.34));

        when(vectorSearchService.search(documentId, question))
                .thenReturn(searchResults);

        when(qaService.ask(any(), eq(question)))
                .thenReturn(answer);

        // when
        DocumentQaResponse documentQaResponse = documentQaService.ask(documentId, question);

        // then
        assertThat(documentQaResponse.answer()).isEqualTo(answer);
        verify(vectorSearchService).search(documentId, question);
        verify(qaService).ask("청크 1\n\n청크 2", question);
    }
}
