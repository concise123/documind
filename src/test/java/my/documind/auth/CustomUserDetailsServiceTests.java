package my.documind.auth;

import my.documind.domain.User;
import my.documind.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTests {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("이메일로 인증 정보를 조회한다")
    void shouldReturnUserDetails_whenEmailExists() {
        // given
        String email = "test@test.com";

        User user = User.builder()
                .email(email)
                .password("encodedPassword")
                .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        // when
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // then
        assertThat(userDetails.getUsername()).isEqualTo(email);
        assertThat(userDetails.getPassword()).isEqualTo("encodedPassword");
    }

    @Test
    @DisplayName("등록되지 않는 이메일로 인증할 수 없다")
    void shouldThrowException_whenEmailDoesNotExist() {
        // given
        String email = "notfound@test.com";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(email))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
