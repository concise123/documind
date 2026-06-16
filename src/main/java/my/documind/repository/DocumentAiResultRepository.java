package my.documind.repository;

import my.documind.domain.AiResultType;
import my.documind.domain.DocumentAiResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentAiResultRepository extends JpaRepository<DocumentAiResult, Long> {
    Optional<DocumentAiResult> findFirstByDocumentIdAndType(Long documentId, AiResultType type);
}
