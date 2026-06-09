package my.documind.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DocumentResponse {
    private Long id;
    private String originalFilename;
    private String contentType;
    private Long fileSize;
}