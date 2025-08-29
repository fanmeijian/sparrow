package cn.sparrowmini.bpm.server.config;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration("kieServerSecurity")
@EnableWebSecurity
public class DefaultWebSecurityConfig extends KeycloakWebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		super.configure(http);
		http.cors().and().csrf().disable().authorizeRequests(authorize -> {
			try {
				authorize
						.antMatchers("/api/**","/browser/**","/v3/**", "/swagger-ui/**", "/rest/swagger-ui/**", "/h2-console/**",
								"/rest/swagger.json", "/rest/api-docs","/rest/api-docs/**", "/rest/lib/**", "/rest/images/**",
								"/rest/css/**", "/rest/swagger*","/rest/swagger.json/**","/v3/api-docs","/bpm/v3/api-docs","/actuator/**")
						.permitAll().anyRequest().authenticated().and().headers().frameOptions().disable();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}).oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt).headers().frameOptions().disable();

	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		KeycloakAuthenticationProvider keycloakAuthenticationProvider = keycloakAuthenticationProvider();
		SimpleAuthorityMapper mapper = new SimpleAuthorityMapper();
		mapper.setPrefix("");
		keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(mapper);
		auth.authenticationProvider(keycloakAuthenticationProvider);
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration corsConfiguration = new CorsConfiguration();
		corsConfiguration.setAllowedOriginPatterns(Arrays.asList("*"));
//		corsConfiguration.setAllowedOrigins(Arrays.asList("*"));
		corsConfiguration.setAllowCredentials(true);
		corsConfiguration.setAllowedMethods(Arrays.asList(HttpMethod.OPTIONS.name(), HttpMethod.GET.name(),
				HttpMethod.HEAD.name(), HttpMethod.POST.name(), HttpMethod.DELETE.name(), HttpMethod.PUT.name(),
				HttpMethod.PATCH.name()));
		corsConfiguration.applyPermitDefaultValues();
		source.registerCorsConfiguration("/**", corsConfiguration);
		return source;
	}

	// Unable to locate Keycloak configuration file: keycloak.json
	@Order(1)
	@Bean
	KeycloakConfigResolver KeycloakConfigResolver() {
		return new KeycloakSpringBootConfigResolver();
	}

	@Bean
	@Override
	protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
		return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());

	}

}
