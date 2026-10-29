package my.documind.common.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;

import static org.assertj.core.api.Assertions.assertThat;

public class ApiMultipartExceptionResolverTests {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ApiMultipartExceptionResolver resolver = new ApiMultipartExceptionResolver(objectMapper);
    private long maxUploadSize = 10 * 1024 * 1024;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    @DisplayName("파일 크기가 제한을 초과하면 업로드할 수 없다")
    void shouldReturnPayloadTooLarge_whenFileSizeExceedsLimit() throws Exception {
        ErrorCode errorCode = ErrorCode.FILE_SIZE_EXCEEDED;

        // given
        request.setRequestURI("/api/v1/document");
        MaxUploadSizeExceededException exception = new MaxUploadSizeExceededException(maxUploadSize);

        // when
        ModelAndView result = resolver.resolveException(request, response, null, exception);

        // then
        assertThat(result).isNotNull();
        assertThat(response.getStatus()).isEqualTo(errorCode.getStatus().value());
        assertThat(response.getContentType()).startsWith(MediaType.APPLICATION_JSON_VALUE);
        ErrorResponse errorResponse = objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);
        assertThat(errorResponse.code()).isEqualTo(errorCode.getCode());
        assertThat(errorResponse.message()).isEqualTo(errorCode.getMessage());
        assertThat(errorResponse.status()).isEqualTo(errorCode.getStatus().value());
    }

    @Test
    @DisplayName("파일 크기가 제한을 초과하면 처리하지 않는다")
    void shouldNotProcessFile_whenFileSizeExceedsLimit() {
        // given
        request.setRequestURI("/document/upload");
        MaxUploadSizeExceededException exception = new MaxUploadSizeExceededException(maxUploadSize);

        // when
        ModelAndView result = resolver.resolveException(request, response, null, exception);

        // then
        assertThat(result).isNull();
    }
}
