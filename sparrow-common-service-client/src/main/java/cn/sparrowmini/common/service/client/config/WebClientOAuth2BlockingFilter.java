package cn.sparrowmini.common.service.client.config;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

public class WebClientOAuth2BlockingFilter {

    public static ExchangeFilterFunction oauth2TokenFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest ->
                ReactiveSecurityContextHolder.getContext()
                        .flatMap(securityContext -> {
                            var auth = securityContext.getAuthentication();
                            if (auth instanceof JwtAuthenticationToken jwtToken) {
                                ClientRequest newRequest = ClientRequest.from(clientRequest)
                                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken.getToken().getTokenValue())
                                        .build();
                                return Mono.just(newRequest);
                            }
                            return Mono.just(clientRequest);
                        })
                        .defaultIfEmpty(clientRequest)  // 确保即使没有securityContext也返回原请求，不阻塞
        );
    }
}