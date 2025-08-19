package cn.sparrowmini.common.service.client;

import cn.sparrowmini.common.CurrentUser;
import cn.sparrowmini.common.exception.NoPermissionException;
import cn.sparrowmini.common.service.ScopePermission;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ScopePermissionAspect {

    @Autowired
    private ScopePermissionServiceClient scopePermissionServiceClient;

    @Around("@annotation(scopePermission)")
    public Object hasPermission(ProceedingJoinPoint joinPoint, ScopePermission scopePermission) throws Throwable {

        final String scope = scopePermission.scope();
        log.debug("username {}, scope code: {}", CurrentUser.get(), scope);
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

//        ObjectMapper mapper = new ObjectMapper();// .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
//        ObjectWriter writer = mapper.writer().withoutAttribute("principal");

        boolean hasPermission= Boolean.TRUE.equals(scopePermissionServiceClient.hasPermission(scope).block());
        if (!hasPermission) {
            throw new NoPermissionException(
                    String.join("-", username, "没有权限", scopePermission.name(), scope));
        } else {
            return joinPoint.proceed();
        }


    }


}
