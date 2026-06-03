package my.documind.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import my.documind.domain.User;
import my.documind.dto.UserLoginDTO;
import my.documind.dto.UserSignupDTO;
import my.documind.service.UserService;
import org.springframework.stereotype.Controller;
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
    public void signupGET() {
        log.info("----------signup get----------");
    }

    @PostMapping("/signup")
    public String signupPOST(UserSignupDTO userSignupDTO, RedirectAttributes redirectAttributes) {
        log.info("----------signup post----------");
        log.info(userSignupDTO);
        try {
            userService.signup(userSignupDTO);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/user/signup";
        }
        redirectAttributes.addFlashAttribute("result", "success");
        return "redirect:/user/login";
    }

    @GetMapping("/login")
    public void LoginGET() throws IOException {
        log.info("----------Login get----------");
    }
}
