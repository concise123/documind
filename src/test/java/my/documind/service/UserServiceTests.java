package my.documind.service;

import my.documind.common.exception.ErrorMessage;
import my.documind.domain.User;
import my.documind.dto.UserLoginDTO;
import my.documind.dto.UserSignupDTO;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        UserSignupDTO userSignupDTO = UserSignupDTO.builder()
                .password("password")
                .email("test@test.com")
                .nickname("tester")
                .build();

        when(userRepository.existsByEmail(userSignupDTO.getEmail()))
                .thenReturn(false);

        when(passwordEncoder.encode(userSignupDTO.getPassword()))
                .thenReturn("encodedPassword");

        userService.signup(userSignupDTO);

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
        UserSignupDTO userSignupDTO = UserSignupDTO.builder()
                .password("password")
                .email("test@test.com")
                .nickname("tester")
                .build();

        when(userRepository.existsByEmail(userSignupDTO.getEmail()))
                .thenReturn(true);

        assertThatThrownBy(() ->
                userService.signup(userSignupDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ErrorMessage.EMAIL_ALREADY_EXISTS.getMessage());

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    @DisplayName("로그인 성공")
    void login_success() {
        UserLoginDTO userLoginDTO =
                UserLoginDTO.builder()
                        .email("test@test.com")
                        .password("password")
                        .build();

        User user = User.builder()
                .email(userLoginDTO.getEmail())
                .password("encodedPassword")
                .build();

        when(userRepository.findByEmail(userLoginDTO.getEmail()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                userLoginDTO.getPassword(),
                user.getPassword()
        )).thenReturn(true);

        userService.login(userLoginDTO);

        verify(userRepository).findByEmail(userLoginDTO.getEmail());

        verify(passwordEncoder)
                .matches(
                        userLoginDTO.getPassword(),
                        user.getPassword()
                );
    }

    @Test
    @DisplayName("존재하지 않는 이메일 로그인 실패")
    void login_fail_user_not_found() {
        UserLoginDTO userLoginDTO =
                UserLoginDTO.builder()
                        .email("notfound@test.com")
                        .password("password")
                        .build();

        when(userRepository.findByEmail(userLoginDTO.getEmail()))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> userService.login(userLoginDTO)
                );

        assertThat(exception.getMessage())
                .isEqualTo(ErrorMessage.USER_NOT_FOUND.getMessage());

        verify(userRepository)
                .findByEmail(userLoginDTO.getEmail());

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("비밀번호 불일치 로그인 실패")
    void login_fail_wrong_password() {
        UserLoginDTO userLoginDTO =
                UserLoginDTO.builder()
                        .email("test@test.com")
                        .password("wrong")
                        .build();

        User user = User.builder()
                .email(userLoginDTO.getEmail())
                .password("encodedPassword")
                .build();

        when(userRepository.findByEmail(userLoginDTO.getEmail()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                userLoginDTO.getPassword(),
                user.getPassword()
        )).thenReturn(false);

        assertThatThrownBy(() ->
                userService.login(userLoginDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ErrorMessage.INVALID_PASSWORD.getMessage());
    }
}
