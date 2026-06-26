package my.documind.repository;

import my.documind.domain.Document;
import my.documind.domain.DocumentStatus;
import my.documind.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class DocumentRepositoryTests {
    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = userRepository.save(createUser("test@test.com"));
    }

    private User createUser(String email) {
        return User.builder()
                .password("password")
                .email(email)
                .nickname("tester")
                .build();
    }

    private Document createDocument() {
        return Document.builder()
                .originalFilename("test.pdf")
                .storedFilename("uuid_test.pdf")
                .contentType("application/pdf")
                .fileSize(100L)
                .user(user)
                .status(DocumentStatus.UPLOADED)
                .build();
    }

    @Test
    @DisplayName("사용자로 문서를 조회한다")
    void findByUser_OrderByRegDateDesc_returnsDocuments_whenOwnerMatches() {
        // given
        Document document = documentRepository.save(createDocument());

        // when
        List<Document> documents = documentRepository.findByUserOrderByRegDateDesc(user);

        // then
        assertThat(documents).hasSize(1);
        assertThat(documents.get(0).getOriginalFilename()).isEqualTo("test.pdf");
    }

    @Test
    @DisplayName("문서 아이디와 사용자로 문서를 조회한다")
    void findByIdAndUser_returnsDocument_whenOwnerMatches() {
        // given
        Document document = documentRepository.save(createDocument());
        Long documentId = document.getId();

        // when
        Optional<Document> result = documentRepository.findByIdAndUser(documentId, user);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(documentId);
        assertThat(result.get().getUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("문서 아이디와 사용자로 문서를 조회한다")
    void findByIdAndUser_returnsEmpty_whenOwnerDoesNotMatch() {
        // given
        Document document = documentRepository.save(createDocument());
        Long documentId = document.getId();
        User anotherUser = userRepository.save(createUser("another@test.com"));

        // when
        Optional<Document> result = documentRepository.findByIdAndUser(documentId, anotherUser);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("문서 아이디와 사용자로 문서를 조회한다")
    void findByIdAndUser_returnsEmpty_whenDocumentDoesNotExist() {
        // when
        Optional<Document> result = documentRepository.findByIdAndUser(1L, user);

        // then
        assertThat(result).isEmpty();
    }
}