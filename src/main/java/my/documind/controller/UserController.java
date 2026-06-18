package my.documind.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import my.documind.exception.EmailAlreadyExistsException;
import my.documind.dto.UserSignupRequest;
import my.documind.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequestMapping("/user")
@Log4j2
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/signup")
    public String signupGET(Model model) {
        log.info("----------signup get----------");
        model.addAttribute("userSignupRequest", new UserSignupRequest());
        return "user/signup";
    }

    @PostMapping("/signup")
    public String signupPOST(@Valid UserSignupRequest userSignupRequest, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        log.info("----------signup post----------");
        log.info(userSignupRequest);
        if (bindingResult.hasErrors()) {
            return "user/signup";
        }
        try {
            userService.signup(userSignupRequest);
        } catch (EmailAlreadyExistsException e) {
            bindingResult.rejectValue(
                    "email",
                    "duplicate",
                    e.getMessage()
            );
            return "user/signup";
        }
        redirectAttributes.addFlashAttribute("message", "회원가입이 완료되었습니다. 로그인해주세요.");
        return "redirect:/user/login";
    }

    @GetMapping("/login")
    public void LoginGET() throws IOException {
        log.info("----------Login get----------");
    }
}
