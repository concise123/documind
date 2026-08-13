package my.documind.document.service;

import lombok.RequiredArgsConstructor;
import my.documind.document.domain.Document;
import my.documind.document.domain.DocumentChunk;
import my.documind.document.repository.DocumentChunkRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class DocumentChunkService {
    private static final int CHUNK_SIZE = 2000;
    private final DocumentChunkRepository chunkRepository;

    public void createChunksIfAbsent(Document document) {
        if (chunkRepository.existsByDocumentId(document.getId())) {
            return;
        }
        createChunks(document);
    }

    public void createChunks(Document document) {
        List<String> chunks = chunk(document.getExtractedText());
        List<DocumentChunk> entities = IntStream.range(0, chunks.size())
                .mapToObj(index -> DocumentChunk.builder()
                                .document(document)
                                .content(chunks.get(index))
                                .chunkIndex(index)
                                .build())
                .toList();
        chunkRepository.saveAll(entities);
    }

    private List<String> chunk(String text) {
        List<String> chunks = new ArrayList<>();
        for (int i = 0; i < text.length(); i += CHUNK_SIZE) {
            int end = Math.min(text.length(), i + CHUNK_SIZE);
            chunks.add(text.substring(i, end));
        }
        return chunks;
    }
}
