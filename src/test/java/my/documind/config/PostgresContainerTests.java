package my.documind.config;

import my.documind.support.AbstractPostgresRepositoryTests;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;

import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PostgresContainerTests extends AbstractPostgresRepositoryTests {
    @Autowired
    DataSource dataSource;

    @Test
    void testConnection() throws Exception {
        try (Connection con = dataSource.getConnection()) {
            assertThat(con.isValid(2)).isTrue();
        }
    }
}
