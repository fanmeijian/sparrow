package cn.sparrowmini.admin;

import cn.sparrowmini.system.audit.AuditService;
import cn.sparrowmini.identity.local.LocalPrincipal;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.*;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.request.*;

/** Successful mutations and audit records commit together. Failed attempts are recorded separately. */
@Aspect @Component
public class ManagementAuditAspect {
    private final TransactionTemplate command,failed;private final AuditService audit;
    public ManagementAuditAspect(PlatformTransactionManager manager,AuditService audit){
        this.audit=audit;command=new TransactionTemplate(manager);failed=new TransactionTemplate(manager);failed.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }
    @Around("execution(public * cn.sparrowmini.web.*Controller.*(..))")
    public Object record(ProceedingJoinPoint invocation)throws Throwable{
        var attributes=RequestContextHolder.getRequestAttributes();
        if(!(attributes instanceof ServletRequestAttributes servlet))return invocation.proceed();
        var request=servlet.getRequest();String method=request.getMethod();
        if(!request.getRequestURI().startsWith("/api/admin/") || java.util.Set.of("GET","HEAD","OPTIONS").contains(method))return invocation.proceed();
        var authentication=SecurityContextHolder.getContext().getAuthentication();
        String subject=authentication!=null && authentication.getPrincipal() instanceof LocalPrincipal p?p.namespace()+":"+p.subjectId():"anonymous";
        String target=request.getRequestURI();String requestId=java.util.UUID.randomUUID().toString();if(servlet.getResponse()!=null)servlet.getResponse().setHeader("X-Request-Id",requestId);
        try{return command.execute(status->{
            try{var result=invocation.proceed();audit.record(subject,method,target,"SUCCEEDED",requestId,result instanceof cn.sparrowmini.web.PolicyController.View policy?policy.activeRevision():null,"AUTHORIZED_COMMAND");return result;}
            catch(Throwable failure){throw new InvocationFailure(failure);}
        });}catch(RuntimeException wrapped){
            Throwable cause=wrapped instanceof InvocationFailure?wrapped.getCause():wrapped;
            String outcome=cause instanceof SecurityException?"DENIED":cause instanceof jakarta.persistence.OptimisticLockException?"CONFLICT":cause instanceof IllegalArgumentException?"INVALID":"FAILED";
            try{failed.executeWithoutResult(status->audit.record(subject,method,target,outcome,requestId,null,outcome));}catch(RuntimeException auditFailure){cause.addSuppressed(auditFailure);}
            throw cause;
        }
    }
    private static final class InvocationFailure extends RuntimeException {InvocationFailure(Throwable cause){super(cause);}}
}
