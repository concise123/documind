package my.documind.service;

import my.documind.common.exception.ErrorMessage;
import my.documind.domain.User;
import my.documind.dto.UserSignupDTO;
import my.documind.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {
    @Mock
    private UserRepository userRepository;

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

        userService.signup(userSignupDTO);

        ArgumentCaptor<User> captor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(captor.capture());

        User savedUser = captor.getValue();

        assertThat(savedUser.getPassword())
                .isEqualTo("password");

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
}
