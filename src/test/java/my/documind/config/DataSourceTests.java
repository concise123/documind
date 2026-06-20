package my.documind.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class DataSourceTests {
    @Autowired
    private DataSource dataSource;

    @Test
    void testConnection() throws Exception {
        try (Connection con = dataSource.getConnection()) {
            assertThat(con.isValid(2)).isTrue();
        }
    }
}
