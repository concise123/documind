package my.documind.document.service;

import lombok.RequiredArgsConstructor;
import my.documind.ai.service.EmbeddingService;
import my.documind.document.util.VectorUtils;
import my.documind.document.domain.DocumentChunk;
import my.documind.document.repository.DocumentChunkRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VectorSearchService {
    private static final int DEFAULT_TOP_K = 5;
    private final EmbeddingService embeddingService;
    private final DocumentChunkRepository chunkRepository;

    public List<DocumentChunk> search(Long documentId, String question) {
        float[] queryEmbedding = embeddingService.embed(question);
        String vector = VectorUtils.toVectorString(queryEmbedding);
        return chunkRepository.findSimilarChunks(documentId, vector, DEFAULT_TOP_K);
    }
}
