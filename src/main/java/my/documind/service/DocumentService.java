package my.documind.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import my.documind.common.exception.*;
import my.documind.domain.Document;
import my.documind.domain.User;
import my.documind.dto.DocumentResponse;
import my.documind.repository.DocumentRepository;
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
    private final DocumentRepository documentRepository;
    private final FileStorageService fileStorageService;
    private final PdfTextExtractor pdfTextExtractor;
    private final UserService userService;

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
                    .extractedText(extractedText)
                    .build();
            documents.add(document);
        }
        documentRepository.saveAll(documents);
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
                        .contentType(document.getContentType())
                        .fileSize(document.getFileSize())
                        .build())
                .toList();
    }
}