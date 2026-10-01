package my.documind.document.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QaRateLimiter {
    private static final String KEY_PREFIX = "rate_limit:qa:";
    private final StringRedisTemplate redisTemplate;
    private final QaRateLimitProperties properties;

    public boolean isAllowed(String email) {
        String key = KEY_PREFIX + email;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == null) {
            return false;
        }
        if (count == 1) {
            redisTemplate.expire(key, properties.window());
        }
        return count <= properties.maxRequests();
    }
}
