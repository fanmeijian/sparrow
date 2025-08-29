package cn.sparrowmini.bpm.server.workitem;

import cn.sparrowmini.bpm.server.workitem.TokenService;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class WebClientService {

    private final WebClient webClient;
    private final TokenService tokenService;

    public WebClientService(WebClient.Builder builder, TokenService tokenService) {
        this.webClient = builder.build();
        this.tokenService = tokenService;
    }

    public <T> Mono<T> get(String url, Class<T> responseType) {
        return tokenService.getToken()
                .flatMap(token -> webClient.get()
                        .uri(url)
                        .headers(h -> h.setBearerAuth(token))
                        .exchangeToMono(response -> handleResponse(response, responseType))
                );
    }

    public <T> Mono<T> post(String url, Object body, Class<T> responseType) {
        return tokenService.getToken()
                .flatMap(token -> webClient.post()
                        .uri(url)
                        .headers(h -> h.setBearerAuth(token))
                        .body(BodyInserters.fromValue(body))
                        .exchangeToMono(response -> handleResponse(response, responseType))
                );
    }

    public <T> Mono<T> put(String url, Object body, Class<T> responseType) {
        return tokenService.getToken()
                .flatMap(token -> webClient.put()
                        .uri(url)
                        .headers(h -> h.setBearerAuth(token))
                        .body(BodyInserters.fromValue(body))
                        .exchangeToMono(response -> handleResponse(response, responseType))
                );
    }

    public <T> Mono<T> patch(String url, Object body, Class<T> responseType) {
        return tokenService.getToken()
                .flatMap(token -> webClient.patch()
                        .uri(url)
                        .headers(h -> h.setBearerAuth(token))
                        .body(BodyInserters.fromValue(body))
                        .exchangeToMono(response -> handleResponse(response, responseType))
                );
    }

    public <T> Mono<T> delete(String url, Class<T> responseType) {
        return tokenService.getToken()
                .flatMap(token -> webClient.delete()
                        .uri(url)
                        .headers(h -> h.setBearerAuth(token))
                        .exchangeToMono(response -> handleResponse(response, responseType))
                );
    }

    private <T> Mono<T> handleResponse(org.springframework.web.reactive.function.client.ClientResponse response, Class<T> responseType) {
        int status = response.statusCode().value();
        if (status >= 400 && status < 500) {
            return response.bodyToMono(String.class).flatMap(body -> Mono.error(new RuntimeException("Client error: " + body)));
        } else if (status >= 500 && status < 600) {
            return response.bodyToMono(String.class).flatMap(body -> Mono.error(new RuntimeException("Server error: " + body)));
        } else {
            return response.bodyToMono(responseType);
        }
    }
}