package my.documind.document.controller.api;

import com.jayway.jsonpath.JsonPath;
import my.documind.common.exception.ErrorCode;
import my.documind.config.ApiAuthenticationEntryPoint;
import my.documind.config.CustomSecurityConfig;
import my.documind.document.domain.Document;
import my.documind.document.dto.DocumentResponse;
import my.documind.document.exception.DocumentNotFoundException;
import my.documind.document.exception.InvalidFileException;
import my.documind.document.service.DocumentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentApiController.class)
@Import({
        CustomSecurityConfig.class,
        ApiAuthenticationEntryPoint.class
})
public class DocumentApiControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DocumentService documentService;

    @Test
    @DisplayName("여러 파일을 업로드하면 생성된 문서 목록을 반환한다")
    @WithMockUser
    void shouldReturnCreatedDocuments_whenUploadingMultipleFiles() throws Exception {
        // given
        MockMultipartFile file1 = new MockMultipartFile(
                "files",
                "document1.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "content1".getBytes()
        );

        MockMultipartFile file2 = new MockMultipartFile(
                "files",
                "document2.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "content2".getBytes()
        );

        Document document1 = Document.builder()
                .id(1L)
                .originalFilename("document1.pdf")
                .build();

        Document document2 = Document.builder()
                .id(2L)
                .originalFilename("document2.pdf")
                .build();

        when(documentService.upload(any(), any()))
                .thenReturn(List.of(document1, document2));

        // when & then
        mockMvc.perform(multipart("/api/v1/document")
                        .file(file1)
                        .file(file2)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].fileName").value("document1.pdf"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].fileName").value("document2.pdf"));
    }

    @Test
    @DisplayName("PDF 형식이 아닌 파일은 업로드할 수 없다")
    @WithMockUser
    void shouldReturnBadRequest_whenFileIsNotPdf() throws Exception {
        ErrorCode errorCode = ErrorCode.INVALID_FILE_TYPE;

        // given
        MockMultipartFile file = new MockMultipartFile(
                "files",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "test".getBytes()
        );

        when(documentService.upload(any(), any()))
                .thenThrow(new InvalidFileException());

        // when & then
        mockMvc.perform(multipart("/api/v1/document").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(errorCode.getCode()))
                .andExpect(jsonPath("$.message").value(errorCode.getMessage()))
                .andExpect(jsonPath("$.status") .value(errorCode.getStatus().value()));
    }

    @Test
    @DisplayName("인증되지 않은 사용자는 문서 목록을 조회할 수 없다")
    void shouldReturnUnauthorized_whenUserIsUnauthenticated() throws Exception {
        ErrorCode errorCode = ErrorCode.AUTHENTICATION_REQUIRED;

        // when & then
        mockMvc.perform(get("/api/v1/document"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(errorCode.getCode()))
                .andExpect(jsonPath("$.message") .value(errorCode.getMessage()))
                .andExpect(jsonPath("$.status") .value(errorCode.getStatus().value()));
    }

    @Test
    @DisplayName("인증된 사용자는 문서를 조회할 수 있다")
    @WithMockUser
    void shouldReturnDocument_whenUserIsAuthenticated() throws Exception {
        // given
        Long documentId = 1L;
        String originalFilename = "test.pdf";
        Long fileSize = 100L;
        LocalDateTime regDate = LocalDateTime.now();
        DocumentResponse response = DocumentResponse.builder()
                .id(documentId)
                .originalFilename(originalFilename)
                .fileSize(fileSize)
                .regDate(regDate)
                .build();

        when(documentService.findDocument(eq(documentId), any()))
                .thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/document/{id}", documentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(documentId))
                .andExpect(jsonPath("$.fileName").value(originalFilename))
                .andExpect(jsonPath("$.fileSize").value(fileSize))
                .andExpect(result -> {
                    String actual = JsonPath.read(result.getResponse().getContentAsString(), "$.regDate");
                    assertThat(LocalDateTime.parse(actual)).isEqualTo(regDate);
                });
    }

    @Test
    @DisplayName("존재하지 않는 문서를 조회할 수 없다")
    @WithMockUser
    void shouldReturnNotFound_whenDocumentDoesNotExist() throws Exception {
        // given
        Long documentId = 999L;

        when(documentService.findDocument(eq(documentId), any()))
                .thenThrow(new DocumentNotFoundException());

        // when & then
        mockMvc.perform(get("/api/v1/document/{id}", documentId))
                .andExpect(status().isNotFound());
    }
}
