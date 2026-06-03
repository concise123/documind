package my.documind.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import my.documind.common.exception.ErrorMessage;
import my.documind.domain.User;
import my.documind.dto.UserLoginDTO;
import my.documind.dto.UserSignupDTO;
import my.documind.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void signup(UserSignupDTO userSignupDTO) {
        if (userRepository.existsByEmail(userSignupDTO.getEmail())) {
            throw new IllegalArgumentException(ErrorMessage.EMAIL_ALREADY_EXISTS.getMessage());
        }
        User user = User.builder()
                .password(userSignupDTO.getPassword())
                .email(userSignupDTO.getEmail())
                .nickname(userSignupDTO.getNickname())
                .build();
        user.changePassword(passwordEncoder.encode(userSignupDTO.getPassword()));
        log.info(user);
        userRepository.save(user);
    }
}
