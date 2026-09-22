package my.documind.document.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import my.documind.common.exception.ErrorCode;
import my.documind.config.ApiAuthenticationEntryPoint;
import my.documind.config.CustomSecurityConfig;
import my.documind.document.dto.DocumentQaRequest;
import my.documind.document.dto.DocumentQaResponse;
import my.documind.document.service.DocumentQaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentQaApiController.class)
@Import({
        CustomSecurityConfig.class,
        ApiAuthenticationEntryPoint.class
})
public class DocumentQaApiControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DocumentQaService documentQAService;

    private Long documentId;

    @BeforeEach
    void setUp() {
        documentId = 1L;
    }

    @Test
    @DisplayName("정상적인 질문을 보내면 답변을 반환한다")
    @WithMockUser
    void shouldReturnAnswer_whenQuestionIsValid() throws Exception {
        // given
        String question = "질문";
        String answer = "답변";
        DocumentQaResponse response = new DocumentQaResponse(question, answer);
        DocumentQaRequest request = new DocumentQaRequest(question);

        when(documentQAService.ask(any(), any(), eq(question)))
                .thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/document/{documentId}/qa", documentId)
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("질문이 비어 있으면 보낼 수 없다")
    @WithMockUser void shouldReturnBadRequest_whenQuestionIsEmpty() throws Exception {
        ErrorCode errorCode = ErrorCode.INVALID_REQUEST;

        // given
        DocumentQaRequest request = new DocumentQaRequest("");

        // when & then
        mockMvc.perform(post("/api/v1/document/{documentId}/qa", documentId)
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(errorCode.getCode()))
                .andExpect(jsonPath("$.message").value("질문을 입력해주세요."))
                .andExpect(jsonPath("$.status").value(errorCode.getStatus().value()));
    }

    @Test
    @DisplayName("잘못된 요청 형식은 처리할 수 없다")
    @WithMockUser
    void shouldReturnBadRequest_whenRequestBodyIsInvalid() throws Exception {
        ErrorCode errorCode = ErrorCode.INVALID_REQUEST;

        // when & then
        mockMvc.perform(post("/api/v1/document/{documentId}/qa", documentId)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                { "question" "질문" }"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(errorCode.getCode()))
                .andExpect(jsonPath("$.message").value(errorCode.getMessage()))
                .andExpect(jsonPath("$.status").value(errorCode.getStatus().value()));
    }

    @Test
    @DisplayName("예상하지 못한 오류가 발생하면 요청을 처리할 수 없다")
    @WithMockUser
    void shouldReturnInternalServerError_whenUnexpectedExceptionOccurs() throws Exception {
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;

        // given
        when(documentQAService.ask(any(), any(), any()))
                .thenThrow(new RuntimeException("Unexpected error"));

        // when & then
        mockMvc.perform(post("/api/v1/document/{documentId}/qa", documentId)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                { "question" : "질문" }"""))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code") .value(errorCode.getCode()))
                .andExpect(jsonPath("$.message") .value(errorCode.getMessage()))
                .andExpect(jsonPath("$.status") .value(errorCode.getStatus().value()));
    }
}
