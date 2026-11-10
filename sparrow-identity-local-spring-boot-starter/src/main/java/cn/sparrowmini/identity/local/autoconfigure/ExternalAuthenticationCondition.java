package cn.sparrowmini.identity.local.autoconfigure;

import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotatedTypeMetadata;
public final class ExternalAuthenticationCondition implements Condition {
    public boolean matches(ConditionContext context,AnnotatedTypeMetadata metadata){
        var env=context.getEnvironment();return !"local".equals(env.getProperty("sparrow.identity.provider","local"))||"external".equals(env.getProperty("sparrow.identity.authentication"));
    }
}
