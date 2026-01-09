package cn.sparrowmini.server.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Value("${application.name}")
	private String applicationName;

	@Value("${application.description}")
	private String applicationDescription;

	@Value("${build.version}")
	private String buildVersion;

	@Value("${build.timestamp}")
	private String buildTimestamp;

	/**
	 * 
	 * @return OpenApi
	 */
	@Bean
	public OpenAPI springShopOpenAPI() {
		SecurityScheme securityScheme = new SecurityScheme();
		securityScheme.setScheme("bearer");
		securityScheme.setType(Type.HTTP);
		securityScheme.setBearerFormat("JWT");
		
		return new OpenAPI()
				.components(new Components().addSecuritySchemes("bearerAuth",securityScheme ))
				.addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
				.info(new Info().title(this.applicationName)
				.description(this.applicationDescription).version(this.buildVersion));
	}
}
