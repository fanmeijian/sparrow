package cn.sparrowmini.common.service.client.config;

import reactor.core.publisher.Mono;

import java.util.function.Supplier;

public class SecureCaller {
    public static <T> T call(Supplier<T> supplier) {
        return ReactiveSecurityContextBridge.withSecurityContext(Mono.fromSupplier(supplier)).block();
    }
}