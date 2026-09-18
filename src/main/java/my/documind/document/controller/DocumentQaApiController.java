package my.documind.document.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.documind.document.dto.DocumentQaRequest;
import my.documind.document.dto.DocumentQaResponse;
import my.documind.document.service.DocumentQaService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/document/{id}/qa")
@RequiredArgsConstructor
public class DocumentQaApiController {
    private final DocumentQaService documentQAService;

    @PostMapping
    @ResponseBody
    public DocumentQaResponse askQuestion(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails,
                                          @Valid @RequestBody DocumentQaRequest request) {
        return documentQAService.ask(id, userDetails.getUsername(), request.getQuestion());
    }
}
