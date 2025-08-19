package cn.sparrowmini.common.service.client.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

@Aspect
@Component
@Slf4j
public class ReactiveSecurityContextAspect {

    @Around("@annotation(cn.sparrowmini.common.service.client.config.WithReactiveSecurityContext)")
    public Object wrapReactiveSecurityContext(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();

        SecurityContext context = SecurityContextHolder.getContext();
        if (context == null || context.getAuthentication() == null) {
            return result;
        }

        if (result instanceof Mono<?> mono) {
            log.debug("Injecting SecurityContext into Reactor Context");
            return mono.contextWrite(Context.of(SecurityContext.class, context));
        }

        // 可扩展：支持 Flux、CompletableFuture 等
        return result;
    }
}