package my.documind;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class DocumindApplication {

	public static void main(String[] args) {
		SpringApplication.run(DocumindApplication.class, args);
	}

}
