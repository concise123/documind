package my.documind.exception;

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
    public String handleDocumentNotFoundException(DocumentNotFoundException e, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        log.error("{} uri={}, method={}", e.getMessage(), request.getRequestURI(), request.getMethod(), e);
        redirectAttributes.addFlashAttribute("message", e.getMessage());
        return "redirect:/document/list";
    }

    @ExceptionHandler(FileEmptyException.class)
    public String handleFileEmptyException(FileEmptyException e, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        log.warn("{} uri={}, method={}", e.getMessage(), request.getRequestURI(), request.getMethod(), e);
        redirectAttributes.addFlashAttribute("message", e.getMessage());
        redirectAttributes.addFlashAttribute("reopenUploadModal", true);
        return "redirect:/document/list";
    }

    @ExceptionHandler(FileException.class)
    public String handleFileException(FileException e, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        log.error("{} uri={}, method={}", e.getMessage(), request.getRequestURI(), request.getMethod(), e);
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
    public String handleInvalidFileException(InvalidFileException e, HttpServletRequest request,
                                             RedirectAttributes redirectAttributes) {
        log.warn("{} uri={}, method={}", e.getMessage(), request.getRequestURI(), request.getMethod(), e);
        redirectAttributes.addFlashAttribute("message", e.getMessage());
        redirectAttributes.addFlashAttribute("reopenUploadModal", true);
        return "redirect:/document/list";
    }

    @ExceptionHandler(UserNotFoundException.class)
    public String handleUserNotFoundException(UserNotFoundException e, HttpServletRequest request,
                                              RedirectAttributes redirectAttributes) {
        log.error("{} uri={}, method={}", e.getMessage(), request.getRequestURI(), request.getMethod(), e);
        request.getSession().invalidate();
        SecurityContextHolder.clearContext();
        redirectAttributes.addFlashAttribute("message", e.getMessage());
        return "redirect:/user/login";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e, HttpServletRequest request,
                                                       RedirectAttributes redirectAttributes) {
        log.warn("{} uri={}, method={}", e.getMessage(), request.getRequestURI(), request.getMethod(), e);
        redirectAttributes.addFlashAttribute("message", ErrorMessage.FILE_SIZE_EXCEEDED.getMessage());
        redirectAttributes.addFlashAttribute("reopenUploadModal", true);
        return "redirect:/document/list";
    }

    @ExceptionHandler(DailyUploadLimitExceededException.class)
    public String handleDailyUploadLimitExceededException(DailyUploadLimitExceededException e, HttpServletRequest request,
                                          RedirectAttributes redirectAttributes) {
        log.warn("{} uri={}, method={}", e.getMessage(), request.getRequestURI(), request.getMethod(), e);
        redirectAttributes.addFlashAttribute("message", e.getErrorMessage().format(e.getArgs()));
        return "redirect:/document/list";

    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        log.error("{} uri={}, method={}", e.getMessage(), request.getRequestURI(), request.getMethod(), e);
        redirectAttributes.addFlashAttribute("message", ErrorMessage.INTERNAL_SERVER_ERROR.getMessage());
        return "redirect:/";
    }
}