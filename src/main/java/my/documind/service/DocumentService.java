package my.documind.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import my.documind.domain.*;
import my.documind.dto.DocumentRequest;
import my.documind.dto.DocumentResponse;
import my.documind.dto.PageResponse;
import my.documind.event.DocumentUploadedEvent;
import my.documind.exception.*;
import my.documind.repository.DocumentAiResultRepository;
import my.documind.repository.DocumentRepository;
import my.documind.upload.PdfBatchRunner;
import my.documind.upload.PdfExtractionResult;
import my.documind.upload.UploadFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentService {
    private static final int PAGE_SIZE = 5;

    private final ApplicationEventPublisher eventPublisher;
    private final DocumentAiResultRepository documentAiResultRepository;
    private final DocumentRepository documentRepository;
    private final FileStorageService fileStorageService;
    private final PdfBatchRunner pdfBatchRunner;
    private final UserService userService;

    @Value("${document.daily-upload-limit}")
    private int dailyUploadLimit;

    /**
     * PDF 문서를 업로드하고 저장한다.
     *
     * <p>업로드된 파일을 저장한 후 텍스트를 추출하고 문서 정보를 DB에 저장한다.
     * 문서 저장이 완료되면 AI 요약 생성을 위해 {@code DocumentUploadedEvent}를 발행한다.</p>
     *
     * @param files 업로드할 PDF 파일
     * @param email 업로드한 사용자 이메일
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
     * @throws DailyUploadLimitExceededException 일일 업로드 제한을 초과한 경우
     * @throws FileEmptyException 빈 파일을 업로드한 경우
     * @throws InvalidFileException PDF 형식이 아닌 파일을 업로드한 경우
     * @throws FileException 파일 읽기 또는 저장에 실패한 경우
     */
    @Transactional
    public void upload(List<MultipartFile> files, String email) {
        log.info("문서 업로드 시작. email={}, fileCount={}", email, files.size());
        User user = userService.getByEmail(email);
        validateDailyUploadLimit(user, files.size());
        List<String> storedFilenames = new ArrayList<>();
        List<UploadFile> uploadFiles = new ArrayList<>();
        List<Document> documents;
        for (MultipartFile file : files) {
            validateFile(file);
            String storedFilename = fileStorageService.store(file);
            storedFilenames.add(storedFilename);
            uploadFiles.add(new UploadFile(file, storedFilename));
        }
        try {
            List<PdfExtractionResult> results = pdfBatchRunner.extractAll(uploadFiles);
            documents = results.stream()
                    .map(result -> {
                        String text = Optional.ofNullable(result.text()).orElse("");
                        return Document.from(result.withText(text), user);
                    })
                    .toList();
        } catch (RuntimeException e) {
            for (String filename : storedFilenames) {
                try {
                    fileStorageService.delete(filename);
                } catch (FileException fe) {
                    log.warn("파일 정리 작업 실패", fe);
                }
            }
            throw e;
        }
        List<Document> savedDocuments = documentRepository.saveAll(documents);
        log.info("문서 업로드 완료. email={}, savedDocumentCount={}", email, savedDocuments.size());
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
    public PageResponse<DocumentResponse> findDocuments(String email, DocumentRequest documentRequest) {
        User user = userService.getByEmail(email);
        int page = documentRequest.getPage();
        String keyword = documentRequest.getKeyword();
        Page<Document> result;
        if (keyword == null || keyword.isBlank()) {
            Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE, Sort.by("regDate").descending());
            result = documentRepository.findByUser(user, pageable);
        } else {
            Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE);
            result = documentRepository.searchByUserAndKeyword(user.getId(), keyword.trim(), pageable);
        }
        List<DocumentResponse> dtoList = result.getContent()
                .stream()
                .map(document -> DocumentResponse.builder()
                        .id(document.getId())
                        .originalFilename(document.getOriginalFilename())
                        .fileSize(document.getFileSize())
                        .regDate(document.getRegDate())
                        .documentRequest(documentRequest)
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
        String summary = documentAiResultRepository
                .findFirstByDocumentIdAndTypeOrderByRegDateDesc(id, AiResultType.SUMMARY)
                .map(DocumentAiResult::getContent)
                .orElse(null);
        return DocumentResponse.builder()
                .id(document.getId())
                .originalFilename(document.getOriginalFilename())
                .fileSize(document.getFileSize())
                .status(document.getStatus())
                .extractedText(document.getExtractedText())
                .summary(summary)
                .retryCount(document.getRetryCount())
                .regDate(document.getRegDate())
                .build();
    }
}