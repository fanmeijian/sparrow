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

@Slf4j
@Aspect
@Component
public class WithSecurityContextBlockingAspect {

    @Around("@annotation(cn.sparrowmini.common.service.client.config.WithSecurityContextBlocking)")
    public Object wrapWithSecurityContext(ProceedingJoinPoint joinPoint) throws Throwable {
        SecurityContext context = SecurityContextHolder.getContext();

        // 包装原方法调用逻辑
        Mono<Object> monoCall = Mono.deferContextual(ctx -> {
            try {
                Object result = joinPoint.proceed();
                return Mono.just(result);
            } catch (Throwable e) {
                return Mono.error(e);
            }
        });

        return monoCall
                .contextWrite(Context.of(SecurityContext.class, context))
                .block(); // 强制 block，但上下文已写入
    }
}