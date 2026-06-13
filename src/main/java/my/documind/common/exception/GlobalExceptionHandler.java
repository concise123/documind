package my.documind.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Log4j2
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(DocumentNotFoundException.class)
    public String handleDocumentNotFoundException(DocumentNotFoundException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("message", e.getMessage());
        return "redirect:/document/list";
    }

    @ExceptionHandler(FileEmptyException.class)
    public String handleFileEmptyException(FileEmptyException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("message", e.getMessage());
        redirectAttributes.addFlashAttribute("reopenUploadModal", true);
        return "redirect:/document/list";
    }

    @ExceptionHandler(FileStorageException.class)
    public String handleFileStorageException(FileStorageException e, RedirectAttributes redirectAttributes) {
        switch (e.getErrorMessage()) {
            case FILE_DELETE_FAILED -> {
                redirectAttributes.addFlashAttribute("message", e.getMessage());
            }
            default -> {
                redirectAttributes.addFlashAttribute("message", e.getMessage());
                redirectAttributes.addFlashAttribute("reopenUploadModal", true);
            }
        }
        return "redirect:/document/list";
    }

    @ExceptionHandler(InvalidFileException.class)
    public String handleInvalidFileException(InvalidFileException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("message", e.getMessage());
        redirectAttributes.addFlashAttribute("reopenUploadModal", true);
        return "redirect:/document/list";
    }

    @ExceptionHandler(UserNotFoundException.class)
    public String handleUserNotFoundException(HttpServletRequest request,
                                              UserNotFoundException e, RedirectAttributes redirectAttributes) {
        request.getSession().invalidate();
        SecurityContextHolder.clearContext();
        redirectAttributes.addFlashAttribute("message", e.getMessage());
        return "redirect:/user/login";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e,
                                                       RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("message", ErrorMessage.FILE_SIZE_EXCEEDED.getMessage());
        redirectAttributes.addFlashAttribute("reopenUploadModal", true);
        return "redirect:/document/list";
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("message", ErrorMessage.INTERNAL_SERVER_ERROR.getMessage());
        return "redirect:/";
    }
}