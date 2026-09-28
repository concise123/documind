package my.documind.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import my.documind.auth.domain.User;
import my.documind.auth.jwt.JwtTokenProvider;
import my.documind.auth.repository.UserRepository;
import my.documind.document.domain.Document;
import my.documind.document.domain.DocumentStatus;
import my.documind.document.repository.DocumentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class ApiSecurityIntegrationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private String createAccessToken(String username) {
        UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername(username)
                .password("password")
                .build();
        return jwtTokenProvider.createAccessToken(userDetails);
    }

    @Test
    @DisplayName("인증 없이 API에 접근할 수 없다")
    void shouldReturnUnauthorized_whenJwtIsMissing() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/document"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("유효하지 않은 JWT로는 인증할 수 없다")
    void shouldReturnUnauthorized_whenJwtIsInvalid() throws Exception {
        // given
        String invalidToken = "invalid.jwt.token";

        // when & then
        mockMvc.perform(get("/api/v1/document")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("유효한 JWT로 인증할 수 있다")
    void shouldAuthenticate_WhenJwtIsValid() throws Exception {
        // given
        String email = "test@test.com";
        User user = User.builder()
                .password("password")
                .email(email)
                .nickname("tester")
                .build();
        Document document = Document.builder()
                .originalFilename("test.pdf")
                .storedFilename("uuid.pdf")
                .contentType("application/pdf")
                .fileSize(100L)
                .user(user)
                .status(DocumentStatus.UPLOADED)
                .build();
        userRepository.save(user);
        documentRepository.save(document);
        String token = createAccessToken(email);

        // when & then
        mockMvc.perform(get("/api/v1/document")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("만료된 JWT로는 인증할 수 없다")
    void shouldReturnUnauthorized_whenJwtIsExpired() throws Exception {
        // given
        String expiredToken = createExpiredToken();

        // when & then
        mockMvc.perform(get("/api/v1/document")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    private String createExpiredToken() {
        SecretKey secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject("test@test.com")
                .issuedAt(Date.from(Instant.now().minusSeconds(3600)))
                .expiration(Date.from(Instant.now().minusSeconds(1800)))
                .signWith(secretKey)
                .compact();
    }
}
