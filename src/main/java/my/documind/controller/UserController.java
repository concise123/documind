package my.documind.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.documind.exception.EmailAlreadyExistsException;
import my.documind.dto.UserSignupRequest;
import my.documind.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/signup")
    public void showSignupForm() {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "회원가입이 비활성화되었습니다.");
    }

/*    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        model.addAttribute("userSignupRequest", new UserSignupRequest());
        return "user/signup";
    }

    @PostMapping("/signup")
    public String signup(@Valid UserSignupRequest userSignupRequest, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
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
    }*/

    @GetMapping("/login")
    public void showLoginForm() throws IOException {
    }
}
