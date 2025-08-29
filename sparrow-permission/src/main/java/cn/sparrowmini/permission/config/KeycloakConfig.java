package cn.sparrowmini.permission.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfig {
	@Bean
	Keycloak keycloak(OAuthProperties props) {
		return KeycloakBuilder.builder() //
				.serverUrl(props.getAuthServerUrl()) //
				.realm(props.getRealm()) //
				.grantType(OAuth2Constants.CLIENT_CREDENTIALS) //
				.clientId(props.getResource()) //
				.clientSecret((String) props.getCredentials().getSecret()) //
				.build();
	}
}
