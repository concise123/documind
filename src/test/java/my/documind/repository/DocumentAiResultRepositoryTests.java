package my.documind.repository;

import my.documind.domain.*;
import my.documind.dto.SummaryResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class DocumentAiResultRepositoryTests {
    @Autowired
    private DocumentAiResultRepository documentAiResultRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("문서 아이디와 타입으로 AI 결과를 조회한다")
    void findFirstByDocumentIdAndType_returnsAiResult_whenAiResultExists() {
        // given
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
                .status(DocumentStatus.UPLOADED)
                .build();
        documentRepository.save(document);

        SummaryResponse response = new SummaryResponse("요약 결과", "gpt-4o-mini", 10);
        DocumentAiResult aiResult = DocumentAiResult.summary(response);
        document.addAiResult(aiResult);
        documentAiResultRepository.save(aiResult);

        // when
        Optional<DocumentAiResult> result = documentAiResultRepository
                .findFirstByDocumentIdAndType(document.getId(), AiResultType.SUMMARY);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getContent()).isEqualTo("요약 결과");
    }
}
