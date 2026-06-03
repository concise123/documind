package my.documind.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserLoginDTO {
    private String password;
    private String email;
}