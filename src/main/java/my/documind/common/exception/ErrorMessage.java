package my.documind.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorMessage {
    // validation (사용자 입력 문제)
    EMAIL_ALREADY_EXISTS("이미 존재하는 이메일입니다. 다른 이메일을 입력해 주세요."),
    FILE_EMPTY("파일을 선택해주세요."),
    INVALID_FILE_TYPE("PDF 파일만 업로드 가능합니다."),
    FILE_SIZE_EXCEEDED("파일 크기가 너무 큽니다."),

    // not found (리소스 없음)
    USER_NOT_FOUND("존재하지 않는 사용자입니다."),
    DOCUMENT_NOT_FOUND("문서를 찾을 수 없습니다."),

    // system (서버 문제)
    FILE_SAVE_FAILED("파일 저장에 실패했습니다."),
    FILE_DELETE_FAILED("파일 삭제에 실패했습니다."),
    INTERNAL_SERVER_ERROR("예상치 못한 오류가 발생했습니다."),
    USER_SESSION_INVALID("다시 로그인해 주세요.");

    private final String message;
}
