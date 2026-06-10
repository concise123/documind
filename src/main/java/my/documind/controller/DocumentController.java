package my.documind.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import my.documind.service.DocumentService;
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
@Log4j2
@RequiredArgsConstructor
public class DocumentController {
    private final DocumentService documentService;

    @PostMapping(value = "/upload")
    public String uploadPOST(@RequestParam List<MultipartFile> files, @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        log.info("----------upload post----------");
        documentService.upload(files, userDetails.getUsername());
        redirectAttributes.addFlashAttribute("message", "문서가 업로드되었습니다.");
        return "redirect:/document/list";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        log.info("----------delete----------");
        documentService.delete(id, userDetails.getUsername());
        redirectAttributes.addFlashAttribute("message", "문서가 삭제되었습니다.");
        return "redirect:/document/list";
    }

    @GetMapping("/list")
    public void list(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        log.info("----------list----------");
        model.addAttribute("documents", documentService.findDocuments(userDetails.getUsername()));
    }
}