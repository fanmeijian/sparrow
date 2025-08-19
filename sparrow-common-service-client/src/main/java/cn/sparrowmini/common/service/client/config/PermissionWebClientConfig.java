package cn.sparrowmini.common.service.client.config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class PermissionWebClientConfig {

    @Autowired
    private WebClientOAuth2Filter webClientOAuth2Filter;

    @Value("${sparrow.permission.url}")
    private String apiBase;


    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(apiBase) // K8s 内 DNS
                .filter(webClientOAuth2Filter.oauth2TokenFilter())
                .build();
    }

    public WebClient webClientBlock() {
        return WebClient.builder()
                .baseUrl(apiBase) // K8s 内 DNS
                .filter(WebClientOAuth2BlockingFilter.oauth2TokenFilter())
                .build();
    }


}