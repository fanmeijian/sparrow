package cn.sparrowmini.keycloak.autoconfigure;

import cn.sparrowmini.identity.keycloak.*;
import cn.sparrowmini.identity.api.*;
import cn.sparrowmini.rbac.keycloak.*;
import cn.sparrowmini.rbac.core.*;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.core.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;

@AutoConfiguration(beforeName={"cn.sparrowmini.identity.local.autoconfigure.IdentitySelectionAutoConfiguration","cn.sparrowmini.rbac.autoconfigure.ExternalRoleGrantAutoConfiguration","cn.sparrowmini.rbac.autoconfigure.RbacSelectionAutoConfiguration"})
@ConditionalOnProperty(name="sparrow.identity.provider",havingValue="keycloak")
@EnableConfigurationProperties(KeycloakProperties.class)
public class KeycloakAutoConfiguration {
    @Bean @ConditionalOnMissingBean(cn.sparrowmini.security.api.CurrentActorProvider.class)
    KeycloakActorProvider keycloakActor(KeycloakProperties p){return new KeycloakActorProvider(p.getIssuer(),p.getRoleClients());}
    @Bean @ConditionalOnMissingBean(JwtDecoder.class)
    JwtDecoder keycloakJwtDecoder(KeycloakProperties p){
        if(p.getAudience()==null||p.getAudience().isBlank())throw new IllegalArgumentException("Expected JWT audience required");
        var decoder=NimbusJwtDecoder.withJwkSetUri(p.getIssuer()+"/protocol/openid-connect/certs").restOperations(http()).build();
        OAuth2TokenValidator<Jwt> audience=jwt->jwt.getAudience().contains(p.getAudience())?OAuth2TokenValidatorResult.success():OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token","Unexpected audience",null));
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefaultWithIssuer(p.getIssuer()),audience));return decoder;
    }
    private static RestTemplate http(){var factory=new SimpleClientHttpRequestFactory();factory.setConnectTimeout(5000);factory.setReadTimeout(10000);return new RestTemplate(factory);}
    @Configuration(proxyBeanMethods=false)
    @ConditionalOnProperty(name="sparrow.keycloak.management-enabled",havingValue="true")
    static class Management {
        @Bean @ConditionalOnMissingBean
        KeycloakAdminClient keycloakAdmin(KeycloakProperties p){
            String expected=UriComponentsBuilder.fromUriString(p.getServerUrl()).pathSegment("realms",p.getRealm()).build().encode().toUriString();
            if(!expected.equals(p.getIssuer()))throw new IllegalArgumentException("Management realm must match authentication issuer");
            return new KeycloakAdminClient(URI.create(p.getServerUrl()),p.getRealm(),p.getManagementClientId(),p.getManagementClientSecret());
        }
        @Bean @ConditionalOnMissingBean(UserDirectoryProvider.class)
        KeycloakUserProvider keycloakUsers(KeycloakAdminClient client,KeycloakProperties p,IdentityManagementGuard guard){return new KeycloakUserProvider(client,p.getIssuer(),p.getNamespace(),guard);}
        @Bean @ConditionalOnProperty(name="sparrow.rbac.provider",havingValue="keycloak")
        @ConditionalOnMissingBean(RoleDirectoryProvider.class)
        KeycloakRoleProvider keycloakRoles(KeycloakAdminClient client,KeycloakProperties p,RbacManagementGuard guard){return new KeycloakRoleProvider(client,p.getIssuer(),p.getNamespace(),p.getRoleClientUuid(),guard);}
    }
}
