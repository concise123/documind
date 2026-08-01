package my.documind.pdf;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import my.documind.common.exception.ErrorMessage;
import my.documind.storage.exception.FileStorageException;
import my.documind.pdf.exception.PdfProcessingBusyException;
import my.documind.storage.UploadFile;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.*;

@Log4j2
@RequiredArgsConstructor
@Component
public class PdfBatchRunner {
    private final PdfTextExtractor pdfTextExtractor;

    @Qualifier("pdfExecutor")
    private final ThreadPoolTaskExecutor pdfExecutor;

    public List<PdfExtractionResult> extractAll(List<UploadFile> uploadFiles) {
        long start = System.nanoTime();
        List<Future<PdfExtractionResult>> futures = uploadFiles.stream()
                .map(this::submitExtraction)
                .toList();
        List<PdfExtractionResult> results = futures.stream()
                .map(this::await)
                .toList();
        long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        log.debug("PDF 전체 추출 시간. duration={}ms", duration);
        return results;
    }

    private Future<PdfExtractionResult> submitExtraction(UploadFile uploadFile) {
        logExecutorStatus(uploadFile);
        try {
            return pdfExecutor.submit(() -> pdfTextExtractor.extractText(uploadFile));
        } catch (RejectedExecutionException e) {
            throw new PdfProcessingBusyException();
        }
    }

    private PdfExtractionResult await(Future<PdfExtractionResult> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(ErrorMessage.PDF_PROCESS_INTERRUPTED.getMessage(), e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof FileStorageException fe) {
                throw fe;
            }
            if (cause instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException(cause);
        }
    }

    private void logExecutorStatus(UploadFile uploadFile) {
        if (!log.isDebugEnabled()) {
            return;
        }
        ThreadPoolExecutor executor = pdfExecutor.getThreadPoolExecutor();
        if (executor == null) {
            return;
        }
        log.debug("PDF 추출 작업 제출. file={}, active={}, pool={}, queue={}",
                uploadFile.file().getOriginalFilename(), pdfExecutor.getActiveCount(),
                pdfExecutor.getPoolSize(), pdfExecutor.getThreadPoolExecutor().getQueue().size());
    }
}
