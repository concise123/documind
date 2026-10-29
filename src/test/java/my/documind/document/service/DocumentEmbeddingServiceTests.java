package my.documind.document.service;

import my.documind.ai.service.EmbeddingService;
import my.documind.document.domain.Document;
import my.documind.document.domain.DocumentChunk;
import my.documind.document.repository.DocumentChunkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DocumentEmbeddingServiceTests {
    @Mock
    private DocumentChunkRepository chunkRepository;

    @Mock
    private EmbeddingService embeddingService;

    @InjectMocks
    private DocumentEmbeddingService documentEmbeddingService;

    private Document document;
    private DocumentChunk chunk1;
    private DocumentChunk chunk2;

    @BeforeEach
    void setUp() {
        document = createDocument();
        chunk1 = createChunk(document, "환불 정책", 0);
        chunk2 = createChunk(document, "반품 규정", 1);
    }

    private Document createDocument() {
        return Document.builder()
                .extractedText("원문 텍스트")
                .build();
    }

    private DocumentChunk createChunk(Document document, String content, int chunkIndex) {
        return DocumentChunk.builder()
                .document(document)
                .content(content)
                .chunkIndex(chunkIndex)
                .build();
    }

    @Test
    @DisplayName("모든 청크에 임베딩을 생성한다")
    void shouldEmbedForEachChunk_whenChunksExist() {
        // given
        float[] embedding1 = {0.1f, 0.2f};
        float[] embedding2 = {0.3f, 0.4f};

        when(chunkRepository.findAllByDocumentIdOrderByChunkIndex(document.getId()))
                .thenReturn(List.of(chunk1, chunk2));

        when(embeddingService.embed(chunk1.getContent()))
                .thenReturn(embedding1);

        when(embeddingService.embed(chunk2.getContent()))
                .thenReturn(embedding2);

        // when
        documentEmbeddingService.createEmbeddingsIfAbsent(document);

        // then
        assertThat(chunk1.getEmbedding()).containsExactly(0.1f, 0.2f);
        assertThat(chunk2.getEmbedding()).containsExactly(0.3f, 0.4f);
        verify(embeddingService, times(2)).embed(anyString());
        verify(chunkRepository).saveAll(List.of(chunk1, chunk2));
    }

    @Test
    @DisplayName("기존 임베딩이 있는 청크는 다시 생성하지 않는다")
    void shouldNotEmbed() {
        // given
        float[] existingEmbedding = {0.1f, 0.2f};
        chunk1.updateEmbedding(existingEmbedding);

        when(chunkRepository.findAllByDocumentIdOrderByChunkIndex(document.getId()))
                .thenReturn(List.of(chunk1, chunk2));

        float[] newEmbedding = {0.3f, 0.4f};

        when(embeddingService.embed(chunk2.getContent()))
                .thenReturn(newEmbedding);

        // when
        documentEmbeddingService.createEmbeddingsIfAbsent(document);

        // then
        assertThat(chunk1.getEmbedding()).containsExactly(0.1f, 0.2f);
        assertThat(chunk2.getEmbedding()).containsExactly(0.3f, 0.4f);
        verify(embeddingService, never()).embed(chunk1.getContent());
        verify(embeddingService, only()).embed(chunk2.getContent());
        verify(chunkRepository).saveAll(List.of(chunk1, chunk2));
    }
}
