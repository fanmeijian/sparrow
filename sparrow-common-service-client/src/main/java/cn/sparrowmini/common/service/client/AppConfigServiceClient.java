package cn.sparrowmini.common.service.client;

import cn.sparrowmini.common.service.client.config.PermissionWebClientConfig;
import cn.sparrowmini.common.service.client.config.WithReactiveSecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AppConfigServiceClient {
    private final PermissionWebClientConfig permissionWebClientConfig;

    @WithReactiveSecurityContext
    public Mono<Boolean> hasPermission(String scope) {
        WebClient webClient = permissionWebClientConfig.webClient();
        return webClient.get()
                .uri("/scopes/" + scope + "/has-permission")
                .retrieve()
                .bodyToMono(Boolean.class);

    }
}
