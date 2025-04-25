package in.syncboard.bulkmail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableR2dbcRepositories
@EnableR2dbcAuditing
public class BulkmailServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BulkmailServiceApplication.class, args);
	}
}
