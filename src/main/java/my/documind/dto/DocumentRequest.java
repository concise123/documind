package my.documind.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentRequest {
    @Builder.Default
    private int page = 1;

    private String keyword;
}
