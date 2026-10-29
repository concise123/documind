package my.documind.document.dto;

import my.documind.document.domain.Document;

import java.time.LocalDateTime;

public record DocumentApiResponse (Long id, String fileName, Long fileSize, LocalDateTime regDate) {
    public static DocumentApiResponse from(Document document) {
        return new DocumentApiResponse(
                document.getId(),
                document.getOriginalFilename(),
                document.getFileSize(),
                document.getRegDate()
        );
    }
}
