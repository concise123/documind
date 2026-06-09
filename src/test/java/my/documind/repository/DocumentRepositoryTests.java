package my.documind.repository;

import my.documind.domain.Document;
import my.documind.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DocumentRepositoryTests {
    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("문서 저장 후 조회 테스트")
    void testInsertAndSelect() {
        User user = User.builder()
                .password("password")
                .email("test@test.com")
                .nickname("tester")
                .build();
        userRepository.save(user);

        Document document = Document.builder()
                .originalFilename("test.pdf")
                .storedFilename("uuid_test.pdf")
                .contentType("application/pdf")
                .fileSize(100L)
                .user(user)
                .build();
        documentRepository.save(document);

        List<Document> documents =
                documentRepository.findByUser(user);

        assertThat(documents).hasSize(1);
        assertThat(documents.get(0).getOriginalFilename()).isEqualTo("test.pdf");
    }
}