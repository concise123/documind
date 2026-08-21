package my.documind.document.service;

import my.documind.ai.service.QaService;
import my.documind.document.domain.DocumentChunk;
import my.documind.document.dto.DocumentQaResponse;
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

    @InjectMocks
    private DocumentQaService documentQaService;

    @Test
    @DisplayName("문서 기반 질문에 대한 답변을 제공한다")
    void shouldReturnAnswer_whenDocumentAndQuestionAreProvided() {
        // given
        Long documentId = 1L;
        String question = "질문";
        String answer = "답변";
        DocumentChunk chunk1 = createChunk("청크 1");
        DocumentChunk chunk2 = createChunk("청크 2");
        List<DocumentChunk> chunks = List.of(chunk1, chunk2);

        when(vectorSearchService.search(documentId, question))
                .thenReturn(chunks);

        when(qaService.ask(any(), eq(question)))
                .thenReturn(answer);

        // when
        DocumentQaResponse documentQaResponse = documentQaService.ask(documentId, question);

        // then
        assertThat(documentQaResponse.answer()).isEqualTo(answer);
        verify(vectorSearchService).search(documentId, question);
        verify(qaService).ask("청크 1\n\n청크 2", question);
    }

    private DocumentChunk createChunk(String content) {
        return new DocumentChunk(null, null, content,0, null);
    }
}
