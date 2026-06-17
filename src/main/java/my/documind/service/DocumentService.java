package my.documind.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import my.documind.common.exception.*;
import my.documind.domain.*;
import my.documind.dto.DocumentResponse;
import my.documind.repository.DocumentRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentService {
    private final ApplicationEventPublisher eventPublisher;
    private final DocumentRepository documentRepository;
    private final FileStorageService fileStorageService;
    private final PdfTextExtractor pdfTextExtractor;
    private final UserService userService;

    /**
     * PDF 문서를 업로드하고 저장한다.
     *
     * <p>업로드된 파일에서 텍스트를 추출한 후 파일을 저장하고 문서 정보를 DB에 저장한다.
     * 문서 저장이 완료되면 AI 요약 생성을 위해 {@code DocumentUploadedEvent}를 발행한다.</p>
     *
     * @param files 업로드할 PDF 파일
     * @param email 업로드한 사용자 이메일
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
     * @throws FileException 파일 읽기 또는 저장에 실패한 경우
     */
    @Transactional
    public void upload(List<MultipartFile> files, String email) {
        User user = userService.getByEmail(email);
        List<Document> documents = new ArrayList<>();
        for (MultipartFile file : files) {
            validateFile(file);
            byte[] fileBytes;
            try {
                fileBytes = file.getBytes();
            } catch (IOException e) {
                throw new FileException(ErrorMessage.FILE_READ_FAILED, e);
            }
            String extractedText = Optional.ofNullable(pdfTextExtractor.extractText(fileBytes)).orElse("");
            String storedFilename = fileStorageService.store(file);
            Document document = Document.builder()
                    .originalFilename(file.getOriginalFilename())
                    .storedFilename(storedFilename)
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .user(user)
                    .status(DocumentStatus.UPLOADED)
                    .extractedText(extractedText)
                    .build();
            documents.add(document);
        }
        List<Document> savedDocuments = documentRepository.saveAll(documents);
        savedDocuments.forEach(document ->
                eventPublisher.publishEvent(new DocumentUploadedEvent(document.getId())));
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
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> findDocuments(String email) {
        User user = userService.getByEmail(email);
        return documentRepository.findByUser(user)
                .stream()
                .map(document -> DocumentResponse.builder()
                        .id(document.getId())
                        .originalFilename(document.getOriginalFilename())
                        .fileSize(document.getFileSize())
                        .regDate(document.getRegDate())
                        .build())
                .toList();
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