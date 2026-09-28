package my.documind.document.controller.web;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.documind.document.dto.DocumentQaRequest;
import my.documind.document.dto.DocumentQaResponse;
import my.documind.document.service.DocumentQaService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Hidden
@Controller
@RequestMapping("/document/{id}/qa")
@RequiredArgsConstructor
public class DocumentQaPageController {
    private final DocumentQaService documentQAService;

    @PostMapping
    @ResponseBody
    public DocumentQaResponse askQuestion(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails,
                                          @Valid @RequestBody DocumentQaRequest request) {
        return documentQAService.ask(id, userDetails.getUsername(), request.getQuestion());
    }
}
