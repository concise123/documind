package my.documind.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorMessage {
    EMAIL_ALREADY_EXISTS("이미 존재하는 이메일입니다. 다른 이메일을 입력해 주세요.");

    private final String message;
}
