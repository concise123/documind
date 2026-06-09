package my.documind.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import my.documind.common.exception.ErrorMessage;
import my.documind.domain.Document;
import my.documind.domain.User;
import my.documind.dto.DocumentResponse;
import my.documind.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final FileStorageService fileStorageService;
    private final UserService userService;

    @Transactional
    public void upload(List<MultipartFile> files, String email) {
        User user = userService.getByEmail(email);
        List<Document> documents = new ArrayList<>();
        for (MultipartFile file : files) {
            validateFile(file);
            String storedFilename = fileStorageService.store(file);
            Document document = Document.builder()
                    .originalFilename(file.getOriginalFilename())
                    .storedFilename(storedFilename)
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .user(user)
                    .build();
            documents.add(document);
        }
        documentRepository.saveAll(documents);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException(ErrorMessage.FILE_EMPTY.getMessage());
        }

        String filename = file.getOriginalFilename();

        if (filename == null || !filename.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_FILE_TYPE.getMessage());
        }

        if (!"application/pdf".equals(file.getContentType())) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_FILE_TYPE.getMessage());
        }
    }

    @Transactional
    public void delete(Long id, String email) {
        User user = userService.getByEmail(email);
        Document document = documentRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessage.DOCUMENT_NOT_FOUND.getMessage()));
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