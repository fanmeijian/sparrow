package cn.sparrowmini.common.service.client.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;
import java.util.List;
import org.springframework.web.reactive.function.client.ClientRequest;

@Component
public class WebClientOAuth2Filter {

    private final List<AuthenticationTokenExtractor> tokenExtractors;

    @Autowired
    public WebClientOAuth2Filter(List<AuthenticationTokenExtractor> tokenExtractors) {
        this.tokenExtractors = tokenExtractors;
    }

    public ExchangeFilterFunction oauth2TokenFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(request -> {
            return Mono.deferContextual(ctx -> {
                var auth = SecurityContextHolder.getContext().getAuthentication();

                if (auth == null || !auth.isAuthenticated()) {
                    return Mono.just(request);
                }

                String token = extractAccessToken(auth);
                if (token == null) {
                    return Mono.just(request);
                }

                ClientRequest newRequest = ClientRequest.from(request)
                        .header("Authorization", "Bearer " + token)
                        .build();
                return Mono.just(newRequest);
            });
        });
    }

    private String extractAccessToken(Authentication authentication) {
        for (AuthenticationTokenExtractor extractor : tokenExtractors) {
            if (extractor.supports(authentication)) {
                return extractor.extractToken(authentication);
            }
        }
        return null;
    }
}