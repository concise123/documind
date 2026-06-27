package my.documind.service;

import my.documind.exception.EmailAlreadyExistsException;
import my.documind.exception.ErrorMessage;
import my.documind.exception.UserNotFoundException;
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
import org.springframework.test.util.ReflectionTestUtils;

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
    @DisplayName("회원가입 요청 시 사용자를 등록한다")
    void shouldRegisterUser_whenRequestIsValid() {
        // given
        UserSignupRequest userSignupRequest = UserSignupRequest.builder()
                .password("password")
                .email("test@test.com")
                .nickname("tester")
                .build();

        when(userRepository.existsByEmail(userSignupRequest.getEmail()))
                .thenReturn(false);

        when(passwordEncoder.encode(userSignupRequest.getPassword()))
                .thenReturn("encodedPassword");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    ReflectionTestUtils.setField(user, "id", 1L);
                    return user;
                });

        // when
        userService.signup(userSignupRequest);

        // then
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User savedUser = captor.getValue();
        assertThat(savedUser.getPassword()).isEqualTo("encodedPassword");
        assertThat(savedUser.getEmail()).isEqualTo("test@test.com");
        assertThat(savedUser.getNickname()).isEqualTo("tester");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("이미 사용 중인 이메일로 가입할 수 없다")
    void shouldThrowException_whenEmailAlreadyExists() {
        // given
        UserSignupRequest userSignupRequest = UserSignupRequest.builder()
                .password("password")
                .email("test@test.com")
                .nickname("tester")
                .build();

        when(userRepository.existsByEmail(userSignupRequest.getEmail()))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() ->
                userService.signup(userSignupRequest))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage(ErrorMessage.EMAIL_ALREADY_EXISTS.getMessage());

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    @DisplayName("등록되지 않은 이메일로 사용자를 조회할 수 없다")
    void shouldThrowException_whenEmailDoesNotExist() {
        // given
        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getByEmail("test@test.com"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(ErrorMessage.USER_SESSION_INVALID.getMessage());
    }
}
