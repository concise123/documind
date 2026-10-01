package my.documind.document.redis;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class QaRateLimiterTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private QaRateLimiter rateLimiter;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String EMAIL_1 = "test1@test.com";
    private static final String EMAIL_2 = "test2@test.com";

    @AfterEach
    void tearDown() {
        redisTemplate.delete("rate_limit:qa:" + EMAIL_1);
        redisTemplate.delete("rate_limit:qa:" + EMAIL_2);
    }

    @Test
    @DisplayName("제한 이내 요청은 허용한다")
    void shouldAllowRequest_whenWithinRateLimit() {
        // when & then
        assertThat(rateLimiter.isAllowed(EMAIL_1)).isTrue();
        assertThat(rateLimiter.isAllowed(EMAIL_1)).isTrue();
        assertThat(rateLimiter.isAllowed(EMAIL_1)).isTrue();
    }

    @Test
    @DisplayName("제한을 초과한 요청은 차단한다")
    void shouldRejectRequest_whenRateLimitIsExceeded() {
        // given
        rateLimiter.isAllowed(EMAIL_1);
        rateLimiter.isAllowed(EMAIL_1);
        rateLimiter.isAllowed(EMAIL_1);

        // when
        boolean allowed = rateLimiter.isAllowed(EMAIL_1);

        // then
        assertThat(allowed).isFalse();
    }

    @Test
    @DisplayName("사용자별로 요청 제한을 독립적으로 적용한다")
    void shouldApplyRateLimitIndependently_whenUsersAreDifferent() {
        // given
        rateLimiter.isAllowed(EMAIL_1);
        rateLimiter.isAllowed(EMAIL_1);
        rateLimiter.isAllowed(EMAIL_1);

        // when
        boolean userAAllowed = rateLimiter.isAllowed(EMAIL_1);
        boolean userBAllowed = rateLimiter.isAllowed(EMAIL_2);

        // then
        assertThat(userAAllowed).isFalse();
        assertThat(userBAllowed).isTrue();
    }

    @Test
    @DisplayName("요청 제한 시간이 지나면 다시 요청할 수 있다")
    void shouldAllowRequestAgain_whenRateLimitWindowExpires() throws InterruptedException {
        // given
        rateLimiter.isAllowed(EMAIL_1);
        rateLimiter.isAllowed(EMAIL_1);
        rateLimiter.isAllowed(EMAIL_1);

        assertThat(rateLimiter.isAllowed(EMAIL_1)).isFalse();

        // when
        Thread.sleep(1_100);

        // then
        assertThat(rateLimiter.isAllowed(EMAIL_1)).isTrue();
    }
}
