package my.documind.service;

import my.documind.common.exception.ErrorMessage;
import my.documind.domain.User;
import my.documind.security.CustomUserDetailsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTests {
    @Mock
    private UserService userService;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("로그인 성공")
    void login_success() {
        String email = "test@test.com";

        User user = User.builder()
                .email(email)
                .password("encodedPassword")
                .build();

        when(userService.getByEmail(email))
                .thenReturn(user);

        UserDetails userDetails =
                customUserDetailsService.loadUserByUsername(email);

        assertThat(userDetails.getUsername()).isEqualTo(email);
        assertThat(userDetails.getPassword()).isEqualTo("encodedPassword");
    }

    @Test
    @DisplayName("존재하지 않는 이메일 로그인 실패")
    void login_fail_user_not_found() {
        String email = "notfound@test.com";

        when(userService.getByEmail(email))
                .thenThrow(new UsernameNotFoundException(ErrorMessage.USER_NOT_FOUND.getMessage()));

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(email))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
