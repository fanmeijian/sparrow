package cn.sparrowmini.common.service.client.config;

import java.lang.annotation.*;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WithReactiveSecurityContext {
}
