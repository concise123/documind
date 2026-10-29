package my.documind.common.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiMultipartExceptionResolver implements HandlerExceptionResolver {
    private final ObjectMapper objectMapper;

    public ApiMultipartExceptionResolver(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
                                         Object handler, Exception ex) {
        if (!request.getRequestURI().startsWith("/api/")) {
            return null;
        }
        Throwable cause = ex;
        while (cause != null) {
            ErrorCode errorCode = null;
            if (cause instanceof MaxUploadSizeExceededException) {
                errorCode = ErrorCode.FILE_SIZE_EXCEEDED;
            } else if (cause instanceof MissingServletRequestPartException) {
                errorCode = ErrorCode.FILE_EMPTY;
            }
            if (errorCode != null) {
                response.setStatus(HttpStatus.PAYLOAD_TOO_LARGE.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                ErrorResponse errorResponse = ErrorResponse.of(errorCode);
                try {
                    objectMapper.writeValue(response.getWriter(), errorResponse);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return new ModelAndView();
            }
            cause = cause.getCause();
        }
        return null;
    }
}
