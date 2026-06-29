package my.documind.dto;

import lombok.Builder;
import lombok.Data;
import my.documind.domain.DocumentStatus;

import java.time.LocalDateTime;

@Data
@Builder
public class DocumentResponse {
    private Long id;
    private String originalFilename;
    private Long fileSize;
    private DocumentStatus status;
    private String extractedText;
    private String summary;
    private LocalDateTime regDate;
    private DocumentRequest documentRequest;
}