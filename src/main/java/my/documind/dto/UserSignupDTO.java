package my.documind.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserSignupDTO {
    private Long id;
    private String password;
    private String email;
    private String nickname;
}
