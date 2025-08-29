package cn.sparrowmini.bpm.server.workitem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

@Service
public class TokenService {

    @Autowired
    private KeycloakConfig keycloakConfig;

    private final WebClient webClient;


    private String accessToken;
    private Instant expiryTime;

    public TokenService(WebClient.Builder builder) {
        this.webClient = builder.build();
    }


    public synchronized Mono<String> getToken() {
        if (accessToken != null && Instant.now().isBefore(expiryTime)) {
            return Mono.just(accessToken);
        }

        final String tokenUri = keycloakConfig.getAuthServerUrl()
                + "/realms/" + keycloakConfig.getRealm()
                + "/protocol/openid-connect/token";

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "client_credentials");
        formData.add("client_id", keycloakConfig.getClientId());
        formData.add("client_secret", keycloakConfig.getClientSecret());

        return webClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED) // ✅ 设置正确的 content-type
                .bodyValue(formData) // ✅ 传 formData 而不是 Map
                .retrieve()
                .bodyToMono(Map.class)
                .map(body -> {
                    this.accessToken = (String) body.get("access_token");
                    Integer expiresIn = (Integer) body.get("expires_in");
                    this.expiryTime = Instant.now().plusSeconds(expiresIn - 30);
                    return this.accessToken;
                });
    }

}
