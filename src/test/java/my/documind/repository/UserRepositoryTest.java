package my.documind.repository;

import lombok.extern.log4j.Log4j2;
import my.documind.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

@SpringBootTest
@Log4j2
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("INSERT 기능 테스트")
    public void testInsert() {
        User user = User.builder()
                .password("password")
                .email("test@test.com")
                .nickname("tester")
                .build();
        User result = userRepository.save(user);
        log.info("ID: " + user.getId());
    }

    @Test
    @DisplayName("SELECT 기능 테스트")
    public void testSelect() {
        Long id = 1L;
        Optional<User> result = userRepository.findById(id);
        User user = result.orElseThrow();
        log.info(user);
    }

    @Test
    @DisplayName("UPDATE 기능 테스트")
    public void testUpdate() {
        Long id = 1L;
        Optional<User> result = userRepository.findById(id);
        User user = result.orElseThrow();
        user.changeNickname("tester2");
        userRepository.save(user);
        log.info(user);
    }

    @Test
    @DisplayName("DELETE 기능 테스트")
    public void testDelete() {
        Long id = 1L;
        userRepository.deleteById(id);
    }
}
