package my.documind.document.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import my.documind.ai.service.EmbeddingService;
import my.documind.document.dto.VectorSearchResult;
import my.documind.document.util.VectorUtils;
import my.documind.document.repository.DocumentChunkRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Log4j2
@Service
@RequiredArgsConstructor
public class VectorSearchService {
    private static final int DEFAULT_TOP_K = 5;
    private final EmbeddingService embeddingService;
    private final DocumentChunkRepository chunkRepository;

    public List<VectorSearchResult> search(Long documentId, String question) {
        long embeddingDuration = 0;
        long vectorSearchDuration = 0;
        String status = "FAILED";
        try {
            long embeddingStart = System.nanoTime();
            float[] queryEmbedding = embeddingService.embed(question);
            embeddingDuration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - embeddingStart);
            String vector = VectorUtils.toVectorString(queryEmbedding);
            long vectorSearchStart = System.nanoTime();
            List<VectorSearchResult> results = chunkRepository.findSimilarChunks(documentId, vector, DEFAULT_TOP_K)
                    .stream()
                    .map(result -> new VectorSearchResult(
                            result.getChunkId(),
                            result.getContent(),
                            result.getChunkIndex(),
                            result.getDistance()
                    ))
                    .toList();
            vectorSearchDuration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - vectorSearchStart);
            status = "SUCCESS";
            return results;
        } finally {
            log.info("RAG 검색 시간. status={}, embeddingDuration={}ms, vectorSearchDuration={}ms",
                    status, embeddingDuration, vectorSearchDuration);
        }
    }
}
