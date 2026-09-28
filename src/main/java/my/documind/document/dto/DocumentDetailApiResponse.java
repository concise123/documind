package my.documind.document.dto;

import java.time.LocalDateTime;

public record DocumentDetailApiResponse(Long id,
                                        String fileName,
                                        Long fileSize,
                                        LocalDateTime regDate,
                                        String summary,
                                        String originalText) {
    public static DocumentDetailApiResponse from(DocumentResponse response) {
        return new DocumentDetailApiResponse(
                response.getId(),
                response.getOriginalFilename(),
                response.getFileSize(),
                response.getRegDate(),
                response.getSummary(),
                response.getExtractedText()
        );
    }
}
