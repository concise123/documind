package my.documind.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // 400 잘못된 요청
    CONTENT_EMPTY("AI_400_001", "문서 내용이 없습니다.", HttpStatus.BAD_REQUEST),
    FILE_EMPTY("FILE_400_001", "파일을 선택해주세요.", HttpStatus.BAD_REQUEST),
    INVALID_FILE_TYPE("FILE_400_002", "PDF 파일만 업로드 가능합니다.", HttpStatus.BAD_REQUEST),

    // 401 권한 없음
    USER_SESSION_INVALID("AUTH_401_001", "다시 로그인해 주세요.", HttpStatus.UNAUTHORIZED),

    // 404 요청한 리소스를 찾을 수 없음
    DOCUMENT_NOT_FOUND("DOC_404_001", "문서를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND("USER_404_001", "존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND),

    // 409 현재 리소스 상태와 요청 간의 충돌
    PDF_PROCESSING_BUSY("PDF_409_001", "현재 업로드 요청이 많습니다. 잠시 후 다시 시도해주세요.", HttpStatus.CONFLICT),
    SUMMARY_ALREADY_PROCESSING("SUMMARY_409_001", "이미 처리 중입니다.", HttpStatus.CONFLICT),
    EMAIL_ALREADY_EXISTS("USER_409_001", "이미 존재하는 이메일입니다. 다른 이메일을 입력해 주세요.", HttpStatus.CONFLICT),

    // 413 콘텐츠 용량 초과
    FILE_SIZE_EXCEEDED("FILE_413_001", "파일 크기가 너무 큽니다.", HttpStatus.PAYLOAD_TOO_LARGE),

    // 429 요청/사용량/동시 처리 한도 초과
    OPEN_AI_CONCURRENCY_LIMIT("AI_429_001", "현재 AI 요청이 많습니다. 잠시 후 다시 시도해주세요.", HttpStatus.TOO_MANY_REQUESTS),
    DAILY_UPLOAD_LIMIT_EXCEEDED("FILE_429_001", "오늘 생성 가능한 문서 수 한도에 도달했습니다.", HttpStatus.TOO_MANY_REQUESTS),
    SUMMARY_RETRY_LIMIT_EXCEEDED("SUMMARY_429_001", "재시도 횟수를 초과했습니다.", HttpStatus.TOO_MANY_REQUESTS),

    // 500 내부 서버 오류
    INTERNAL_SERVER_ERROR("COMMON_500_001", "예상치 못한 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_SAVE_FAILED("FILE_500_001", "파일 저장에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_READ_FAILED("FILE_500_002", "파일 읽기에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_DELETE_FAILED("FILE_500_003", "파일 삭제에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    PDF_TEXT_EXTRACTION_FAILED("PDF_500_001", "텍스트 추출에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    PDF_PROCESS_INTERRUPTED("PDF_500_002", "텍스트 추출 작업이 중단되었습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus status;
}
