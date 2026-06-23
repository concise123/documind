package my.documind.controller;

import lombok.RequiredArgsConstructor;
import my.documind.service.DocumentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/document")
@RequiredArgsConstructor
public class DocumentController {
    private final DocumentService documentService;

    @Value("${document.daily-upload-limit}")
    private int dailyUploadLimit;

    @PostMapping(value = "/upload")
    public String uploadDocuments(@RequestParam List<MultipartFile> files, @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        documentService.upload(files, userDetails.getUsername());
        redirectAttributes.addFlashAttribute("message", "문서가 업로드되었습니다.");
        return "redirect:/document/list";
    }

    @PostMapping("/delete/{id}")
    public String deleteDocument(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        documentService.delete(id, userDetails.getUsername());
        redirectAttributes.addFlashAttribute("message", "문서가 삭제되었습니다.");
        return "redirect:/document/list";
    }

    @GetMapping("/list")
    public void showDocuments(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String email = userDetails.getUsername();
        model.addAttribute("dailyUploadLimit", dailyUploadLimit);
        model.addAttribute("todayUploadCount", documentService.getTodayUploadCount(email));
        model.addAttribute("documents", documentService.findDocuments(email));
    }

    @GetMapping("/detail/{id}")
    public String showDocument(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("document", documentService.findDocument(id, userDetails.getUsername()));
        return "document/detail";
    }
}