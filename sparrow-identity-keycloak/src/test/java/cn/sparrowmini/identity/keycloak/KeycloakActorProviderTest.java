package cn.sparrowmini.identity.keycloak;

import org.junit.jupiter.api.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.access.AccessDeniedException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class KeycloakActorProviderTest {
    final String issuer="https://identity.test/realms/demo";
    final KeycloakActorProvider provider=new KeycloakActorProvider(issuer,Set.of("backend"));
    @AfterEach void clear(){SecurityContextHolder.clearContext();}
    Jwt.Builder jwt(String issuer){return Jwt.withTokenValue("verified-test-token").header("alg","RS256").issuer(issuer).subject("subject-123");}
    void authenticate(Jwt jwt){SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt,List.of()));}
    @Test void keepsRoleOriginsAndCaseAndRestrictsClients(){
        authenticate(jwt(issuer).claim("realm_access",Map.of("roles",List.of("Admin"))).claim("resource_access",Map.of("backend",Map.of("roles",List.of("Admin")),"other",Map.of("roles",List.of("Admin")))).claim("roles",List.of("SUPERUSER")).build());
        var actor=provider.current();
        assertEquals(Set.of("realm:Admin",KeycloakActorProvider.clientRole("backend","Admin")),actor.roles());
        assertEquals(KeycloakActorProvider.subject(issuer,"subject-123"),actor.subject());
    }
    @Test void rejectsAnonymousUnauthenticatedAndDifferentIssuer(){
        assertThrows(AccessDeniedException.class,provider::current);
        authenticate(jwt("https://other.test/realms/demo").build());assertThrows(AccessDeniedException.class,provider::current);
        authenticate(jwt(issuer).build());SecurityContextHolder.getContext().getAuthentication().setAuthenticated(false);
        assertThrows(AccessDeniedException.class,provider::current);
    }
    @Test void stableSubjectIgnoresMutableUsernameAndDistinguishesIssuers(){
        authenticate(jwt(issuer).claim("preferred_username","first").build());var id=provider.current().subject();
        authenticate(jwt(issuer).claim("preferred_username","renamed").build());assertEquals(id,provider.current().subject());
        assertNotEquals(id,KeycloakActorProvider.subject("https://other.test/realms/demo","subject-123"));
    }
    @Test void rejectsUnsafeIssuerConfiguration(){
        assertThrows(IllegalArgumentException.class,()->new KeycloakActorProvider("https://user:secret@identity.test/realms/demo",Set.of()));
    }
}
