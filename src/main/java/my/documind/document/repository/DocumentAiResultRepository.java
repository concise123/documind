package my.documind.document.repository;

import my.documind.document.domain.AiResultType;
import my.documind.document.domain.DocumentAiResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentAiResultRepository extends JpaRepository<DocumentAiResult, Long> {
    Optional<DocumentAiResult> findFirstByDocumentIdAndTypeOrderByRegDateDesc(Long documentId, AiResultType type);
}
