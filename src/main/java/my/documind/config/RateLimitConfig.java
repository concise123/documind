package my.documind.config;

import my.documind.document.redis.QaRateLimitProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(QaRateLimitProperties.class)
public class RateLimitConfig {
}
