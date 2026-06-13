package my.documind.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DocumentResponse {
    private Long id;
    private String originalFilename;
    private Long fileSize;
    private String extractedText;
    private LocalDateTime regDate;
}