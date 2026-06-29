package my.documind.repository;

import my.documind.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class UserRepositoryTests {
    @Autowired
    private UserRepository userRepository;

    private String email = "test@example.com";

    @BeforeEach
    void setUp() {
        userRepository.save(createUser(email));
    }

    private User createUser(String email) {
        return User.builder()
                .password("password")
                .email(email)
                .nickname("tester")
                .build();
    }

    @Test
    @DisplayName("이메일 존재 여부를 확인한다")
    void existsByEmail_returnsTrue_whenEmailExists() {
        // when
        boolean exists = userRepository.existsByEmail(email);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("이메일 존재 여부를 확인한다")
    void existsByEmail_returnsFalse_whenEmailDoesNotExist() {
        // when
        boolean exists = userRepository.existsByEmail("notfound@example.com");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("이메일로 사용자를 조회한다")
    void findByEmail_returnsUser_whenEmailExists() {
        // when
        Optional<User> result = userRepository.findByEmail(email);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("이메일로 사용자를 조회한다")
    void findByEmail_returnsEmpty_whenEmailDoesNotExist() {
        // when
        Optional<User> result = userRepository.findByEmail("notfound@example.com");

        // then
        assertThat(result).isEmpty();
    }
}
