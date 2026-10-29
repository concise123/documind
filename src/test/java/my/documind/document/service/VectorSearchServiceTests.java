package my.documind.document.service;

import my.documind.ai.service.EmbeddingService;
import my.documind.document.dto.VectorSearchResult;
import my.documind.document.repository.DocumentChunkRepository;
import my.documind.document.repository.projection.VectorSearchProjection;
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
import static org.mockito.Mockito.*;

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
        float[] embedding = {1.0f, 0.0f, 0.0f};
        VectorSearchProjection similar = mock(VectorSearchProjection.class);
        when(similar.getChunkId()).thenReturn(1L);
        when(similar.getContent()).thenReturn("환불 및 반품 정책");
        when(similar.getChunkIndex()).thenReturn(0);
        when(similar.getDistance()).thenReturn(0.12);
        VectorSearchProjection different = mock(VectorSearchProjection.class);
        when(different.getChunkId()).thenReturn(2L);
        when(different.getContent()).thenReturn("환불 신청 방법");
        when(different.getChunkIndex()).thenReturn(1);
        when(different.getDistance()).thenReturn(0.34);

        when(embeddingService.embed("환불 정책"))
                .thenReturn(embedding);

        when(chunkRepository.findSimilarChunks(eq(documentId), anyString(), eq(5)))
                .thenReturn(List.of(similar, different));

        // when
        List<VectorSearchResult> result = vectorSearchService.search(documentId, "환불 정책");

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).chunkId()).isEqualTo(1L);
        assertThat(result.get(0).chunkIndex()).isEqualTo(0);
        assertThat(result.get(0).distance()).isEqualTo(0.12);
        assertThat(result.get(0).content()).isEqualTo("환불 및 반품 정책");
        assertThat(result.get(1).chunkId()).isEqualTo(2L);
        assertThat(result.get(1).chunkIndex()).isEqualTo(1);
        assertThat(result.get(1).distance()).isEqualTo(0.34);
        assertThat(result.get(1).content()).isEqualTo("환불 신청 방법");
        verify(embeddingService).embed("환불 정책");
        verify(chunkRepository).findSimilarChunks(eq(documentId), anyString(), eq(5));
    }
}
