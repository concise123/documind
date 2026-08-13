package my.documind.document.service;

import lombok.RequiredArgsConstructor;
import my.documind.ai.service.EmbeddingService;
import my.documind.document.domain.Document;
import my.documind.document.domain.DocumentChunk;
import my.documind.document.repository.DocumentChunkRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentEmbeddingService {
    private final DocumentChunkRepository chunkRepository;
    private final EmbeddingService embeddingService;

    public void createEmbeddingsIfAbsent(Document document) {
        List<DocumentChunk> chunks = chunkRepository.findAllByDocumentIdOrderByChunkIndex(document.getId());
        for (DocumentChunk chunk : chunks) {
            if (chunk.hasEmbedding()) {
                continue;
            }
            float[] embedding = embeddingService.embed(chunk.getContent());
            chunk.updateEmbedding(embedding);
        }
        chunkRepository.saveAll(chunks);
    }
}
