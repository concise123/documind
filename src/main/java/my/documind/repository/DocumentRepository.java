package my.documind.repository;

import my.documind.domain.Document;
import my.documind.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    Page<Document> findByUser(User user, Pageable pageable);
    Optional<Document> findByIdAndUser(Long id, User user);
    long countByUserAndRegDateAfter(User user, LocalDateTime regDate);
    @Query(value = """
    SELECT *
    FROM documents
    WHERE user_id = :userId
    AND search_vector @@ plainto_tsquery('simple', :keyword)
    ORDER BY ts_rank(
        search_vector,
        plainto_tsquery('simple', :keyword)
    ) DESC, regdate DESC
    """,
            countQuery = """
    SELECT COUNT(*)
    FROM documents
    WHERE user_id = :userId
    AND search_vector @@ plainto_tsquery('simple', :keyword)
    """,
            nativeQuery = true)
    Page<Document> searchByUserAndKeyword(@Param("userId") Long userId, @Param("keyword") String keyword, Pageable pageable);
}