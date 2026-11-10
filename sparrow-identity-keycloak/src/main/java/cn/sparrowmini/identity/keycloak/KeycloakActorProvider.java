package cn.sparrowmini.identity.keycloak;

import cn.sparrowmini.security.api.*;
import cn.sparrowmini.identity.api.ExternalAuthenticationBridge;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.access.AccessDeniedException;
import java.net.URI;
import java.util.*;
import java.nio.charset.StandardCharsets;

/** Consumes an authenticated JWT; the host filter chain verifies signature, expiry and audience. */
public final class KeycloakActorProvider implements CurrentActorProvider, ExternalAuthenticationBridge {
    private final String issuer;
    private final Set<String> clients;
    public KeycloakActorProvider(String issuer, Set<String> clients) {
        this.issuer=Objects.requireNonNull(issuer);this.clients=Set.copyOf(clients);validateConfiguration();
    }
    @Override public void validateConfiguration() {
        URI uri=URI.create(issuer);
        if (!Set.of("http","https").contains(uri.getScheme()) || uri.getHost()==null || uri.getRawUserInfo()!=null || uri.getRawQuery()!=null || uri.getRawFragment()!=null)
            throw new IllegalArgumentException("Absolute issuer without credentials, query or fragment required");
        if(clients.stream().anyMatch(String::isBlank))throw new IllegalArgumentException("Blank client identifier");
    }
    @Override public ActorContext current() {
        var authentication=SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken token) || !token.isAuthenticated())
            throw new AccessDeniedException("Verified Keycloak token required");
        var jwt=token.getToken();
        if(jwt.getIssuer()==null || !issuer.equals(jwt.getIssuer().toString()) || jwt.getSubject()==null || jwt.getSubject().isBlank())
            throw new AccessDeniedException("Unexpected issuer or missing subject");
        Set<String> roles=new HashSet<>();
        Object realm=jwt.getClaim("realm_access");
        if(realm instanceof Map<?,?> map) collect(roles,"realm:",map.get("roles"));
        Object access=jwt.getClaim("resource_access");
        if(access instanceof Map<?,?> map) for(String client:clients) {
            Object value=map.get(client);
            if(value instanceof Map<?,?> details)collect(roles,"client:"+encode(client)+":",details.get("roles"));
        }
        return new ActorContext(subject(issuer,jwt.getSubject()),roles,Set.of());
    }
    public static String subject(String issuer,String sub){
        if(Objects.requireNonNull(issuer).isBlank() || Objects.requireNonNull(sub).isBlank())throw new IllegalArgumentException("Blank identity");
        return encode(issuer)+"."+encode(sub);
    }
    public static String clientRole(String client,String role){return "client:"+encode(client)+":"+role;}
    private static String encode(String value){return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));}
    private static void collect(Set<String> result,String prefix,Object value){
        if(value instanceof Collection<?> roles) for(Object role:roles)
            if(role instanceof String name && !name.isBlank())result.add(prefix+name);
    }
}
