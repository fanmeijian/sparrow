package cn.sparrowmini.file.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EntityScan({ "cn.sparrowmini.file" })
@ComponentScan({ "cn.sparrowmini.file" })
@EnableJpaRepositories({ "cn.sparrowmini.file" })
@SpringBootApplication
public class TestApplication {
	public static void main(String[] args) {
		SpringApplication.run(TestApplication.class, args);

	}
}
