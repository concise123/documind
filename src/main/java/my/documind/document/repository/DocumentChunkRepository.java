package my.documind.document.repository;

import my.documind.document.domain.DocumentChunk;
import my.documind.document.repository.projection.VectorSearchProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {
    boolean existsByDocumentId(Long documentId);
    List<DocumentChunk> findAllByDocumentIdOrderByChunkIndex(Long documentId);
    @Query(value = """
            SELECT
                dc.id AS chunkId,
                dc.content AS content,
                dc.chunk_index AS chunkIndex,
                dc.embedding <=> CAST(:embedding AS vector) AS distance
            FROM document_chunks dc
            WHERE dc.document_id = :documentId
            ORDER BY dc.embedding <=> CAST(:embedding AS vector)
            LIMIT :limit
            """, nativeQuery = true)
    List<VectorSearchProjection> findSimilarChunks(@Param("documentId") Long documentId, @Param("embedding") String embedding,
                                                   @Param("limit") int limit);
}
