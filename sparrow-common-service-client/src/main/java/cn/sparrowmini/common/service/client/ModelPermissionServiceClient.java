package cn.sparrowmini.common.service.client;

import cn.sparrowmini.common.constant.PermissionEnum;
import cn.sparrowmini.common.service.client.config.PermissionWebClientConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ModelPermissionServiceClient {
    private final PermissionWebClientConfig permissionWebClientConfig;

    /**
     * 增加规则引擎的判断
     *
     * @param modelId
     * @param permission
     * @param entityId
     * @return
     */
    public Mono<Boolean> hasPermission(String modelId, PermissionEnum permission, Object entityId) {
        WebClient webClient = permissionWebClientConfig.webClient();
        return webClient.post()
                .uri("/models/" + modelId + "/permissions/" + permission + "/has-permission")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new PermissionRequest(entityId))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new AccessDeniedException(errorBody)))
                )
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new IllegalStateException("远程服务异常: " + errorBody)))
                )
                .bodyToMono(Boolean.class);
    }

    public record PermissionRequest(Object entityId) {
    }

}
