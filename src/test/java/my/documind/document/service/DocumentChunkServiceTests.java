package my.documind.document.service;

import my.documind.document.domain.Document;
import my.documind.document.domain.DocumentChunk;
import my.documind.document.repository.DocumentChunkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DocumentChunkServiceTests {
    @Mock
    private DocumentChunkRepository chunkRepository;
    @InjectMocks
    private DocumentChunkService chunkService;

    private Document document;

    @BeforeEach
    void setUp() {
        document = Document.builder()
                .extractedText("a".repeat(4500))
                .build();
    }

    @Test
    @DisplayName("문서 내용을 Chunk로 정상적으로 분할한다")
    void shouldSplitTextIntoChunks_whenDocumentContentIsValid() {
        // when
        chunkService.createChunks(document);

        // then
        ArgumentCaptor<List<DocumentChunk>> captor = ArgumentCaptor.forClass(List.class);
        verify(chunkRepository).saveAll(captor.capture());
        List<DocumentChunk> chunks = captor.getValue();
        assertThat(chunks).hasSize(3);
        assertThat(chunks.get(0).getChunkIndex()).isEqualTo(0);
        assertThat(chunks.get(1).getChunkIndex()).isEqualTo(1);
        assertThat(chunks.get(2).getChunkIndex()).isEqualTo(2);
        assertThat(chunks.get(0).getContent()).hasSize(2000);
        assertThat(chunks.get(1).getContent()).hasSize(2000);
        assertThat(chunks.get(2).getContent()).hasSize(500);
    }

    @Test
    @DisplayName("Chunk 크기가 최대 크기를 초과하지 않도록 분할한다")
    void shouldNotExceedChunkSize_whenDocumentContentIsChunked() {
        // when
        chunkService.createChunks(document);

        // then
        ArgumentCaptor<List<DocumentChunk>> captor = ArgumentCaptor.forClass(List.class);
        verify(chunkRepository).saveAll(captor.capture());
        List<DocumentChunk> chunks = captor.getValue();
        assertThat(chunks).allSatisfy(chunk ->
                assertThat(chunk.getContent()).hasSizeLessThanOrEqualTo(2000));
    }

    @Test
    @DisplayName("이미 Chunk가 존재하면 중복으로 생성하지 않는다")
    void shouldNotCreateDuplicateChunks_whenChunksAlreadyExist() {
        // given
        when(chunkRepository.existsByDocumentId(document.getId()))
                .thenReturn(false, true); // 처음 호출되면 false를 반환, 두 번째 호출되면 true를 반환

        // when
        chunkService.createChunksIfAbsent(document);
        chunkService.createChunksIfAbsent(document);

        // then
        verify(chunkRepository, times(2)).existsByDocumentId(document.getId());
        verify(chunkRepository, times(1)).saveAll(anyList());
    }
}
