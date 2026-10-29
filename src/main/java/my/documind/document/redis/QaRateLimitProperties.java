package my.documind.document.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.rate-limit.qa")
public record QaRateLimitProperties(int maxRequests, Duration window) {
}
