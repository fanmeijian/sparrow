package cn.sparrowmini.common.service.client.config;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

public class ReactiveSecurityContextBridge {

    public static <T> Mono<T> withSecurityContext(Mono<T> mono) {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context == null || context.getAuthentication() == null) {
            return mono;
        }
        return mono.contextWrite(Context.of(SecurityContext.class, context));
    }
}