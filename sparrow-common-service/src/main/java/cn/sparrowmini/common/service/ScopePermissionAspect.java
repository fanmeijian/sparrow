package cn.sparrowmini.common.service;

import java.util.Set;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.sparrowmini.common.CurrentUser;
import cn.sparrowmini.common.exception.NoPermissionException;
import cn.sparrowmini.common.repository.ScopeRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
public class ScopePermissionAspect {

	@Autowired
	private ScopeRepository scopeRepository;

    @Around("@annotation(scopePermission)")
    public Object hasPermission(ProceedingJoinPoint joinPoint, ScopePermission scopePermission) throws Throwable {

    	final String username = CurrentUser.get();
    	final Set<String> roles = CurrentUser.getUserInfo().getRoles();
        final String scope = scopePermission.scope();
        log.debug("username {}, scope code: {}", CurrentUser.get(), scope);
        
        boolean hasPermission= true;
        
        if(scopeRepository.hasUserPermission(username, scope) || scopeRepository.hasSysrolePermission(roles, scope) ) {
        	hasPermission=true;
        }else if(scopeRepository.isConfigUserScope(scope) || scopeRepository.isConfigSysroleScope(scope)){
        	hasPermission=false;
        }else {
        	hasPermission=true;
        }
        
        if (!hasPermission) {
            throw new NoPermissionException(
                    String.join("-", CurrentUser.get(), "没有权限", scopePermission.name(), scope));
        } else {
            return joinPoint.proceed();
        }


    }


}
