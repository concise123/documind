package my.documind.document.service;

import my.documind.ai.service.EmbeddingService;
import my.documind.document.domain.Document;
import my.documind.document.domain.DocumentChunk;
import my.documind.document.repository.DocumentChunkRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class VectorSearchServiceTests {
    @Mock
    private EmbeddingService embeddingService;

    @Mock
    private DocumentChunkRepository chunkRepository;

    @InjectMocks
    private VectorSearchService vectorSearchService;

    @Test
    @DisplayName("질문을 임베딩하여 유사한 청크를 조회한다")
    void shouldCreateQueryEmbeddingAndSearchChunks_whenSearchingByQuestion() {
        // given
        Long documentId = 1L;
        Document document = createDocument(documentId);
        float[] embedding = {1.0f, 0.0f, 0.0f};
        List<DocumentChunk> chunks = List.of(
                createChunk(document, "환불 및 반품 정책", 0, null),
                createChunk(document, "환불 신청 방법", 1, null));

        when(embeddingService.embed("환불 정책"))
                .thenReturn(embedding);

        when(chunkRepository.findSimilarChunks(eq(documentId), anyString(), eq(5)))
                .thenReturn(chunks);

        // when
        List<DocumentChunk> result = vectorSearchService.search(documentId, "환불 정책");

        // then
        assertThat(result).isEqualTo(chunks);
        verify(embeddingService).embed("환불 정책");
        verify(chunkRepository).findSimilarChunks(eq(documentId), anyString(), eq(5));
    }

    private Document createDocument(Long documentId) {
        return Document.builder()
                .id(documentId)
                .build();
    }

    private DocumentChunk createChunk(Document document, String content, int chunkIndex, float[] embedding) {
        DocumentChunk chunk = DocumentChunk.builder()
                .document(document)
                .content(content)
                .chunkIndex(chunkIndex)
                .build();
        if (embedding != null) {
            chunk.updateEmbedding(embedding);
        }
        return chunk;
    }
}
