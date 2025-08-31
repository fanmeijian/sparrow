package cn.sparrowmini.rules;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
@SpringBootApplication
public class RuleApplication {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		SpringApplication.run(RuleApplication.class, args);

	}


}
