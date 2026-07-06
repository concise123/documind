package my.documind.upload;

import lombok.RequiredArgsConstructor;
import my.documind.exception.ErrorMessage;
import my.documind.exception.FileException;
import my.documind.service.PdfTextExtractor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RequiredArgsConstructor
@Component
public class PdfBatchRunner {
    private final PdfTextExtractor pdfTextExtractor;

    @Qualifier("pdfExecutor")
    private final ThreadPoolTaskExecutor pdfExecutor;

    public List<PdfExtractionResult> extractAll(List<UploadFile> uploadFiles) {
        List<Future<PdfExtractionResult>> futures = uploadFiles.stream()
                .map(this::submitExtraction)
                .toList();
        return futures.stream()
                .map(this::await)
                .toList();
    }

    private Future<PdfExtractionResult> submitExtraction(UploadFile uploadFile) {
        return pdfExecutor.submit(() -> pdfTextExtractor.extractText(uploadFile));
    }

    private PdfExtractionResult await(Future<PdfExtractionResult> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(ErrorMessage.PDF_PROCESS_INTERRUPTED.getMessage(), e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof FileException fe) {
                throw fe;
            }
            if (cause instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException(cause);
        }
    }
}
