package my.documind.auth.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;

public class JwtTokenProviderTests {
    private static final String SECRET = "this-is-a-test-secret-key-that-is-long-enough-for-hmac";

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties(SECRET, 3_600_000);
        jwtTokenProvider = new JwtTokenProvider(properties);
    }

    @Test
    @DisplayName("사용자 정보로 Access Token을 생성한다")
    void shouldCreateAccessToken_whenUserInfoIsValid() {
        // given
        UserDetails userDetails = User.withUsername("test@test.com")
                .password("password")
                .build();

        // when
        String token = jwtTokenProvider.createAccessToken(userDetails);

        // then
        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.getUsername(token)).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("유효한 Access Token을 검증한다")
    void shouldValidateToken_whenTokenIsValid() {
        // given
        UserDetails userDetails = User.withUsername("test@test.com")
                .password("password")
                .build();
        String token = jwtTokenProvider.createAccessToken(userDetails);

        // when
        boolean result = jwtTokenProvider.validateToken(token);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("변조된 Access Token은 검증하지 않는다")
    void shouldRejectToken_whenTokenIsTampered() {
        // given
        UserDetails userDetails = User.withUsername("test@test.com")
                .password("password")
                .build();
        String token = jwtTokenProvider.createAccessToken(userDetails);
        String tamperedToken = token.substring(0, token.length() - 1) + "x";

        // when
        boolean result = jwtTokenProvider.validateToken(tamperedToken);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("다른 Secret으로 생성된 Access Token은 검증하지 않는다")
    void shouldRejectToken_whenTokenIsSignedWithDifferentSecret() {
        // given
        JwtTokenProvider otherProvider = new JwtTokenProvider(
                new JwtProperties("another-test-secret-key-that-is-long-enough", 3_600_000));
        UserDetails userDetails = User.withUsername("test@test.com")
                .password("password")
                .build();
        String token = otherProvider.createAccessToken(userDetails);

        // when
        boolean result = jwtTokenProvider.validateToken(token);

        // then
        assertThat(result).isFalse();
    }
}
