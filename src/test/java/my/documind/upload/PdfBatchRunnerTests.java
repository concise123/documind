package my.documind.upload;

import my.documind.service.PdfTextExtractor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class PdfBatchRunnerTests {
    @Mock
    private PdfTextExtractor pdfExtractor;

    @Mock
    private ThreadPoolTaskExecutor pdfExecutor;

    @InjectMocks
    private PdfBatchRunner batchRunner;

    @Test
    void extract_returnsExtractionResults() {
        // given
        when(pdfExecutor.submit(any(Callable.class)))
                .thenAnswer(invocation -> {
                    Callable<PdfExtractionResult> task = invocation.getArgument(0);
                    FutureTask<PdfExtractionResult> future = new FutureTask<>(task);
                    future.run();
                    return future;
                });

        UploadFile file = mock(UploadFile.class);
        UploadFile anotherFile = mock(UploadFile.class);

        PdfExtractionResult result = new PdfExtractionResult(file, "text");
        PdfExtractionResult anotherResult = new PdfExtractionResult(anotherFile, "another text");

        when(pdfExtractor.extractText(file))
                .thenReturn(result);

        when(pdfExtractor.extractText(anotherFile))
                .thenReturn(anotherResult);

        // when
        List<PdfExtractionResult> results = batchRunner.extractAll(List.of(file, anotherFile));

        // then
        verify(pdfExtractor).extractText(file);
        verify(pdfExtractor).extractText(anotherFile);
        assertThat(results).containsExactly(result, anotherResult);
    }
}
