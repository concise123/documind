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

    @GetMapping("/upload")
    public void uploadGET() {
        log.info("----------upload get----------");
    }

    @PostMapping(value = "/upload")
    public String uploadPOST(@RequestParam List<MultipartFile> files, @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        log.info("----------upload post----------");
        try {
            documentService.upload(files, userDetails.getUsername());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/document/upload";
        }
        redirectAttributes.addFlashAttribute("result", "success");
        redirectAttributes.addFlashAttribute("action", "upload");
        return "redirect:/document/list";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        log.info("----------delete----------");
        try {
            documentService.delete(id, userDetails.getUsername());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/document/list";
        }
        redirectAttributes.addFlashAttribute("result", "success");
        redirectAttributes.addFlashAttribute("action", "delete");
        return "redirect:/document/list";
    }

    @GetMapping("/list")
    public void list(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        log.info("----------list----------");
        model.addAttribute("documents", documentService.findDocuments(userDetails.getUsername()));
    }
}