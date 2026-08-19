package my.documind.document.repository;

import my.documind.auth.domain.User;
import my.documind.auth.repository.UserRepository;
import my.documind.document.domain.Document;
import my.documind.document.domain.DocumentChunk;
import my.documind.document.domain.DocumentStatus;
import my.documind.document.util.VectorUtils;
import my.documind.support.AbstractPostgresRepositoryTests;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class DocumentChunkRepositoryFtsTests extends AbstractPostgresRepositoryTests {
    @Autowired
    DocumentChunkRepository chunkRepository;

    @Autowired
    DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("유사도가 높은 Chunk부터 조회한다")
    void shouldReturnChunksInSimilarityOrder_whenSearchingSimilarChunks() {
        // given
        Document document = createDocument();
        DocumentChunk similar = createChunk(document, "비슷한 내용", 0, embedding(0));
        DocumentChunk different = createChunk(document, "다른 내용", 1, embedding(1));
        chunkRepository.saveAll(List.of(similar, different));
        String queryEmbedding = VectorUtils.toVectorString(embedding(0));

        // when
        List<DocumentChunk> result = chunkRepository.findSimilarChunks(document.getId(), queryEmbedding, 2);

        // then
        assertThat(result).containsExactly(similar, different);
    }

    @Test
    @DisplayName("상위 K개의 청크만 조회한다")
    void shouldReturnOnlyTopKChunks_whenSearchingSimilarChunks() {
        // given
        Document document = createDocument();
        DocumentChunk chunk1 = createChunk(document, "내용 1", 0, embedding(0));
        DocumentChunk chunk2 = createChunk(document, "내용 2", 1, embedding(1));
        DocumentChunk chunk3 = createChunk(document, "내용 3", 2, embedding(2));
        chunkRepository.saveAll(List.of(chunk1, chunk2, chunk3));
        String queryEmbedding = VectorUtils.toVectorString(embedding(0));

        // when
        List<DocumentChunk> result = chunkRepository.findSimilarChunks(document.getId(), queryEmbedding, 2);

        // then
        assertThat(result).hasSize(2);
    }

    private Document createDocument() {
        User user = userRepository.save(User.builder()
                .password("password")
                .email("test@test.com")
                .nickname("tester")
                .build());
        return documentRepository.save(Document.builder()
                .originalFilename("test.pdf")
                .storedFilename("uuid.pdf")
                .contentType("application/pdf")
                .fileSize(100L)
                .user(user)
                .status(DocumentStatus.UPLOADED)
                .build());
    }

    private DocumentChunk createChunk(Document document, String content, int chunkIndex, float[] embedding) {
        DocumentChunk chunk = DocumentChunk.builder()
                .document(document)
                .content(content)
                .chunkIndex(chunkIndex)
                .build();
        if (embedding != null) {
            chunk.updateEmbedding(embedding);
        }
        return chunk;
    }

    private float[] embedding(int index) {
        float[] embedding = new float[1536];
        embedding[index] = 1.0f;
        return embedding;
    }
}
