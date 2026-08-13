package my.documind.document.repository;

import my.documind.document.domain.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {
    boolean existsByDocumentId(Long documentId);
    List<DocumentChunk> findAllByDocumentIdOrderByChunkIndex(Long documentId);
}
