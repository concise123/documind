package my.documind.service;

import my.documind.common.exception.EmailAlreadyExistsException;
import my.documind.common.exception.ErrorMessage;
import my.documind.common.exception.UserNotFoundException;
import my.documind.domain.User;
import my.documind.dto.UserSignupRequest;
import my.documind.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("회원가입 성공")
    void signup_success() {
        UserSignupRequest userSignupRequest = UserSignupRequest.builder()
                .password("password")
                .email("test@test.com")
                .nickname("tester")
                .build();

        when(userRepository.existsByEmail(userSignupRequest.getEmail()))
                .thenReturn(false);

        when(passwordEncoder.encode(userSignupRequest.getPassword()))
                .thenReturn("encodedPassword");

        userService.signup(userSignupRequest);

        ArgumentCaptor<User> captor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(captor.capture());

        User savedUser = captor.getValue();

        assertThat(savedUser.getPassword())
                .isEqualTo("encodedPassword");

        assertThat(savedUser.getEmail())
                .isEqualTo("test@test.com");

        assertThat(savedUser.getNickname())
                .isEqualTo("tester");

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("중복 이메일 회원가입 실패")
    void signup_duplicate_email() {
        UserSignupRequest userSignupRequest = UserSignupRequest.builder()
                .password("password")
                .email("test@test.com")
                .nickname("tester")
                .build();

        when(userRepository.existsByEmail(userSignupRequest.getEmail()))
                .thenReturn(true);

        assertThatThrownBy(() ->
                userService.signup(userSignupRequest))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage(ErrorMessage.EMAIL_ALREADY_EXISTS.getMessage());

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    @DisplayName("사용자가 없으면 UserNotFoundException 발생")
    void getByEmail_user_not_found() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getByEmail("test@test.com"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(ErrorMessage.USER_NOT_FOUND.getMessage());
    }
}
