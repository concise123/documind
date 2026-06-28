package my.documind.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import my.documind.config.MemoryLogger;
import my.documind.domain.*;
import my.documind.dto.DocumentResponse;
import my.documind.dto.PageResponse;
import my.documind.exception.*;
import my.documind.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentService {
    private static final int PAGE_SIZE = 5;

    private final ApplicationEventPublisher eventPublisher;
    private final DocumentRepository documentRepository;
    private final FileStorageService fileStorageService;
    private final MemoryLogger memoryLogger;
    private final PdfTextExtractor pdfTextExtractor;
    private final UserService userService;

    @Qualifier("pdfExecutor")
    private final ThreadPoolTaskExecutor pdfExecutor;

    @Value("${document.daily-upload-limit}")
    private int dailyUploadLimit;

    /**
     * PDF 문서를 업로드하고 저장한다.
     *
     * <p>업로드된 파일을 하나씩 순회하며 저장하고 각 파일에 대한 텍스트 추출 작업을 비동기로 Future에 제출한다.
     * 모든 Future의 결과를 수집한 후 문서 정보를 DB에 저장한다.
     * 문서 저장이 완료되면 AI 요약 생성을 위해 {@code DocumentUploadedEvent}를 발행한다.</p>
     *
     * @param files 업로드할 PDF 파일
     * @param email 업로드한 사용자 이메일
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
     * @throws FileException 파일 읽기 또는 저장에 실패한 경우
     */
    @Transactional
    public void upload(List<MultipartFile> files, String email) {
        log.info("문서 업로드 시작. email={}, fileCount={}", email, files.size());
        User user = userService.getByEmail(email);
        validateDailyUploadLimit(user, files.size());
        List<Document> documents = new ArrayList<>();
        List<Future<String>> futures = new ArrayList<>();
        List<String> storedFilenames = new ArrayList<>();
        boolean failed = false;
        for (MultipartFile file : files) {
            validateFile(file);
            byte[] fileBytes;
            try {
                fileBytes = file.getBytes();
            } catch (IOException e) {
                throw new FileException(ErrorMessage.FILE_READ_FAILED, e);
            }
            storedFilenames.add(fileStorageService.store(file));
            Future<String> future = pdfExecutor.submit(() -> {
                memoryLogger.logMemory("PDF 추출 시작. file=" + file.getOriginalFilename());
                String text = pdfTextExtractor.extractText(fileBytes);
                memoryLogger.logMemory("PDF 추출 완료. file=" + file.getOriginalFilename()
                        + ", length=" + text.length());
                return text;
            });
            futures.add(future);
        }
        try {
            for (int i = 0; i < files.size(); i++) {
                String storedFilename = storedFilenames.get(i);
                try {
                    MultipartFile file = files.get(i);
                    String text = futures.get(i).get();
                    Document document = Document.builder()
                            .originalFilename(file.getOriginalFilename())
                            .storedFilename(storedFilename)
                            .contentType(file.getContentType())
                            .fileSize(file.getSize())
                            .user(user)
                            .status(DocumentStatus.UPLOADED)
                            .extractedText(text == null ? "" : text)
                            .build();
                    documents.add(document);
                } catch (InterruptedException e) {
                    failed = true;
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(ErrorMessage.PDF_PROCESS_INTERRUPTED.getMessage(), e);
                } catch (ExecutionException e) {
                    failed = true;
                    Throwable cause = e.getCause();
                    if (cause instanceof FileException fe) {
                        throw fe;
                    }
                    if (cause instanceof RuntimeException re) {
                        throw re;
                    }
                    throw new RuntimeException(cause);
                } catch (Exception e) {
                    failed = true;
                    throw new RuntimeException(e);
                }
            }
        } finally {
            if (failed) {
                for (String filename : storedFilenames) {
                    try {
                        fileStorageService.delete(filename);
                    } catch (Exception e) {
                        log.warn("파일 정리 작업 실패", e);
                    }
                }
            }
        }
        List<Document> savedDocuments = documentRepository.saveAll(documents);
        log.info("문서 업로드 완료. email={}, savedDocumentCount={}", email, savedDocuments.size());
        memoryLogger.logMemory("문서 업로드 완료.");
        savedDocuments.forEach(document ->
                eventPublisher.publishEvent(new DocumentUploadedEvent(document.getId())));
        log.debug("이벤트 발행 완료. email={}", email);
    }

    public long getTodayUploadCount(String email) {
        User user = userService.getByEmail(email);
        return getTodayUploadCount(user);
    }

    private void validateDailyUploadLimit(User user, int fileCount) {
        long uploadCount = getTodayUploadCount(user);
        long totalUploadCount = uploadCount + fileCount;
        if (totalUploadCount > dailyUploadLimit) {
            throw new DailyUploadLimitExceededException();
        }
    }

    private long getTodayUploadCount(User user) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        return documentRepository.countByUserAndRegDateAfter(user, startOfDay);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileEmptyException();
        }

        String filename = file.getOriginalFilename();

        if (filename == null || !filename.toLowerCase().endsWith(".pdf")) {
            throw new InvalidFileException();
        }

        if (!"application/pdf".equals(file.getContentType())) {
            throw new InvalidFileException();
        }
    }

    @Transactional
    public void delete(Long id, String email) {
        User user = userService.getByEmail(email);
        Document document = documentRepository.findByIdAndUser(id, user)
                .orElseThrow(DocumentNotFoundException::new);
        fileStorageService.delete(document.getStoredFilename());
        documentRepository.delete(document);
        log.info("문서 삭제 완료. email={}, documentId={}", email, id);
    }

    @Transactional(readOnly = true)
    public PageResponse<DocumentResponse> findDocuments(String email, int page) {
        User user = userService.getByEmail(email);
        Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE, Sort.by("regDate").descending());
        Page<Document> result = documentRepository.findByUser(user, pageable);
        List<DocumentResponse> dtoList = result.getContent()
                .stream()
                .map(document -> DocumentResponse.builder()
                        .id(document.getId())
                        .originalFilename(document.getOriginalFilename())
                        .fileSize(document.getFileSize())
                        .regDate(document.getRegDate())
                        .build())
                .toList();
        return PageResponse.<DocumentResponse>withAll()
                .page(page)
                .size(PAGE_SIZE)
                .total((int)result.getTotalElements())
                .dtoList(dtoList)
                .build();
    }

    @Transactional(readOnly = true)
    public DocumentResponse findDocument(Long id, String email) {
        User user = userService.getByEmail(email);
        Document document = documentRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new DocumentNotFoundException());
        String summary = document.getAiResults().stream()
                .filter(aiResult -> aiResult.getType() == AiResultType.SUMMARY)
                .map(DocumentAiResult::getContent)
                .findFirst()
                .orElse(null);
        return DocumentResponse.builder()
                .id(document.getId())
                .originalFilename(document.getOriginalFilename())
                .fileSize(document.getFileSize())
                .status(document.getStatus())
                .extractedText(document.getExtractedText())
                .summary(summary)
                .regDate(document.getRegDate())
                .build();
    }
}