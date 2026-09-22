package my.documind.document.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import my.documind.common.dto.PageApiResponse;
import my.documind.document.domain.Document;
import my.documind.document.dto.DocumentApiResponse;
import my.documind.document.dto.DocumentDetailApiResponse;
import my.documind.document.dto.DocumentRequest;
import my.documind.document.dto.DocumentResponse;
import my.documind.document.service.DocumentService;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Document", description = "문서 관리 API")
@RestController
@RequestMapping("/api/v1/document")
@RequiredArgsConstructor
public class DocumentApiController {
    private final DocumentService documentService;

    @Operation(summary = "문서 업로드", description = "사용자가 제공한 문서를 업로드합니다.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<DocumentApiResponse>> uploadDocuments(@RequestParam List<MultipartFile> files,
                                                     @AuthenticationPrincipal UserDetails userDetails) {
        List<Document> documents = documentService.upload(files, userDetails.getUsername());
        List<DocumentApiResponse> responses = documents.stream()
                .map(DocumentApiResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "문서 삭제", description = "지정한 문서를 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        documentService.delete(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "문서 목록 조회", description = "현재 사용자의 문서 목록을 페이지 단위로 조회합니다.")
    @GetMapping
    public ResponseEntity<PageApiResponse<DocumentApiResponse>> getDocuments(
            @AuthenticationPrincipal UserDetails userDetails, DocumentRequest documentRequest) {
        String email = userDetails.getUsername();
        Page<Document> page = documentService.findDocuments(email, documentRequest);
        return ResponseEntity.ok(toPageResponse(page));
    }

    private PageApiResponse<DocumentApiResponse> toPageResponse(Page<Document> page) {
        List<DocumentApiResponse> content = page.getContent()
                .stream()
                .map(DocumentApiResponse::from)
                .toList();
        return PageApiResponse.<DocumentApiResponse>builder()
                .page(page.getNumber() + 1)
                .pageSize(page.getSize())
                .totalElements((int)page.getTotalElements())
                .content(content)
                .build();
    }

    @Operation(summary = "문서 상세 조회", description = "문서 정보와 요약 내용을 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<DocumentDetailApiResponse> getDocument(@PathVariable Long id,
                                                                 @AuthenticationPrincipal UserDetails userDetails) {
        DocumentResponse response = documentService.findDocument(id, userDetails.getUsername());
        return ResponseEntity.ok(DocumentDetailApiResponse.from(response));
    }
}