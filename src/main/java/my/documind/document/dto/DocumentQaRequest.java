package my.documind.document.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class DocumentQaRequest {
    @NotBlank(message = "질문을 입력해주세요.")
    @Size(max = 500)
    private String question;

    public String getQuestion() {
        return question;
    }
}
