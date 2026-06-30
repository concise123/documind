package my.documind.repository;

import my.documind.domain.Document;
import my.documind.domain.DocumentStatus;
import my.documind.domain.User;
import my.documind.support.AbstractPostgresRepositoryTests;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class DocumentRepositoryFtsTests extends AbstractPostgresRepositoryTests {
    @Autowired
    DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("키워드가 포함된 문서만 조회한다.")
    void searchByUserAndKeyword_returnsMatchingDocuments_whenKeywordExists() {
        // given
        User user = userRepository.save(User.builder()
                .password("password")
                .email("test@test.com")
                .nickname("tester")
                .build());
        Pageable pageable = PageRequest.of(0, 10);
        saveDocument("Spring Boot Guide.pdf", user, "Spring Boot 입문", LocalDateTime.of(2026, 1, 1, 0, 0));
        saveDocument("Java Collection.pdf", user, "List Set Map", LocalDateTime.of(2026, 1, 2, 0, 0));
        saveDocument("Spring Security.pdf", user, "인증과 인가", LocalDateTime.of(2026, 1, 3, 0, 0));

        // when
        Page<Document> result = documentRepository.searchByUserAndKeyword(user.getId(), "Spring", pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).extracting(Document::getOriginalFilename)
                .containsExactly("Spring Boot Guide.pdf", "Spring Security.pdf");
    }

    private Document saveDocument(String originalFilename, User user, String extractedText, LocalDateTime regDate) {
        Document document = Document.builder()
                .originalFilename(originalFilename)
                .storedFilename(UUID.randomUUID() + ".pdf")
                .contentType("application/pdf")
                .fileSize(100L)
                .user(user)
                .status(DocumentStatus.UPLOADED)
                .extractedText(extractedText)
                .build();
        ReflectionTestUtils.setField(document, "regDate", regDate);
        ReflectionTestUtils.setField(document, "modDate", regDate);
        return documentRepository.save(document);
    }
}
