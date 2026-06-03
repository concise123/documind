package my.documind.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorMessage {
    EMAIL_ALREADY_EXISTS("이미 존재하는 이메일입니다. 다른 이메일을 입력해 주세요."),
    USER_NOT_FOUND("존재하지 않는 사용자입니다."),
    INVALID_PASSWORD("비밀번호가 일치하지 않습니다.");

    private final String message;
}
