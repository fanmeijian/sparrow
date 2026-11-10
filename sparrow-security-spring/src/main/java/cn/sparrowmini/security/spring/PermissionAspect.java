package cn.sparrowmini.security.spring;

import cn.sparrowmini.security.api.*;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import java.util.Objects;
@Aspect
public final class PermissionAspect {
    private final CurrentActorProvider actors;private final FunctionPermissionEvaluator permissions;
    public PermissionAspect(CurrentActorProvider actors,FunctionPermissionEvaluator permissions){this.actors=Objects.requireNonNull(actors);this.permissions=Objects.requireNonNull(permissions);}
    @Around("@annotation(required)")
    public Object require(ProceedingJoinPoint call,RequiresPermission required)throws Throwable{
        if(!permissions.evaluate(actors.current(),new PermissionRequest(required.resource(),required.scope())).allowed())throw new SecurityException("Function permission denied");
        return call.proceed();
    }
}
