package my.documind.document.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.documind.document.dto.DocumentQaRequest;
import my.documind.document.dto.DocumentQaResponse;
import my.documind.document.service.DocumentQaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Document QA", description = "문서 기반 질의응답 API")
@RestController
@RequestMapping("/api/v1/document/{id}/qa")
@RequiredArgsConstructor
public class DocumentQaApiController {
    private final DocumentQaService documentQAService;

    @Operation(summary = "문서 QA", description = "특정 문서를 대상으로 RAG 기반 질문을 수행합니다")
    @PostMapping
    @ResponseBody
    public ResponseEntity<DocumentQaResponse> askQuestion(@PathVariable Long id,
                                                          @AuthenticationPrincipal UserDetails userDetails,
                                                          @Valid @RequestBody DocumentQaRequest request) {
        DocumentQaResponse responses = documentQAService.ask(id, userDetails.getUsername(), request.getQuestion());
        return ResponseEntity.ok(responses);
    }
}
