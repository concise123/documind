package my.documind.document.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentRequest {
    @Min(1)
    @Builder.Default
    private int page = 1;

    private String keyword;
}
