package cn.sparrowmini.identity.keycloak;

import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestClientException;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;
import java.time.*;
import java.util.*;

/** Fixed-realm Admin REST transport. Credentials and upstream bodies never appear in errors. */
public final class KeycloakAdminClient {
    private final RestOperations http;
    private final URI base;
    private final String realm, clientId, secret;
    private String token;
    private Instant expires=Instant.EPOCH;
    public KeycloakAdminClient(RestOperations http,URI base,String realm,String clientId,String secret){
        this.http=Objects.requireNonNull(http);this.base=Objects.requireNonNull(base);
        this.realm=required(realm);this.clientId=required(clientId);this.secret=required(secret);
        if(!Set.of("http","https").contains(base.getScheme()) || base.getHost()==null || base.getUserInfo()!=null || base.getQuery()!=null || base.getFragment()!=null)
            throw new IllegalArgumentException("Invalid Keycloak base URI");
    }
    private static String required(String value){if(value==null||value.isBlank())throw new IllegalArgumentException("Required Keycloak configuration missing");return value;}
    private synchronized String token(){
        if(token!=null && Instant.now().isBefore(expires))return token;
        var body=new LinkedMultiValueMap<String,String>();body.add("grant_type","client_credentials");body.add("client_id",clientId);body.add("client_secret",secret);
        var headers=new HttpHeaders();headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        try {
            var uri=UriComponentsBuilder.fromUri(base).pathSegment("realms",realm,"protocol","openid-connect","token").build().encode().toUri();
            Map<?,?> response=http.exchange(uri,HttpMethod.POST,new HttpEntity<>(body,headers),Map.class).getBody();
            if(response==null || !(response.get("access_token") instanceof String value) || value.isBlank() || !(response.get("expires_in") instanceof Number ttl) || ttl.longValue()<=0)
                throw new IllegalStateException("Invalid Keycloak token response");
            token=value;expires=Instant.now().plusSeconds(Math.max(0,ttl.longValue()-30));return token;
        }catch(RestClientException error){throw new IllegalStateException("Keycloak service authentication failed");}
    }
    public <T> ResponseEntity<T> request(HttpMethod method,List<String> path,Map<String,?> query,Object body,Class<T> type){
        var builder=UriComponentsBuilder.fromUri(base).pathSegment("admin","realms",realm);
        path.forEach(p->{if(p==null||p.isBlank()||p.equals(".")||p.equals(".."))throw new IllegalArgumentException("Invalid path segment");builder.pathSegment(p);});
        query.forEach(builder::queryParam);
        var headers=new HttpHeaders();headers.setBearerAuth(token());headers.setContentType(MediaType.APPLICATION_JSON);
        try{return http.exchange(builder.build().encode().toUri(),method,new HttpEntity<>(body,headers),type);}
        catch(RestClientException error){throw new IllegalStateException("Keycloak management request failed");}
    }
}
