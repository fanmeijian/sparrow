package cn.sparrowmini.rbac.autoconfigure;

import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotatedTypeMetadata;
public final class ExternalRoleCondition implements Condition {
    public boolean matches(ConditionContext context,AnnotatedTypeMetadata metadata){return !"local".equals(context.getEnvironment().getProperty("sparrow.rbac.provider","local"));}
}
