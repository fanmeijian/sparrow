package cn.sparrowmini.audit;

import cn.sparrowmini.common.CurrentUser;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@Slf4j
@Aspect
public class RequestAuditLogAspect {
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
            // 建议：可选，让日期不以 [2026,1,16] 这种数组形式显示
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    RequestAuditLogRepository requestAuditLogRepository;
//
//	@Pointcut("execution(public * *(..))")
//	public void publicMethod() {
//		System.out.println("public method");
//	}

//	@Around("publicMethod() && @annotation(auditLog)")
//	public Object LogExecutionTimeByMethod(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
//		System.out.println(joinPoint + "1 -> " + auditLog);
//		return joinPoint.proceed();
//	}

    //	@Around("publicMethod() && @within(auditLog)")
    @Around("@annotation(auditLog)")
    public Object LogExecutionTimeByClass(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        String actor = "anonymous";
        String origin = "unidentified";

//		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//		if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
//			actor = ((UserDetails) authentication.getPrincipal()).getUsername();
//		}
        actor = CurrentUser.get();

        try {
            origin = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()
                    .getRemoteAddr();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
//		log.debug("{} {} {} {}", joinPoint, joinPoint.getArgs(), actor, origin);
        try {
            requestAuditLogRepository.save(new RequestAuditLog(actor, origin,
                    joinPoint.toString(), objectMapper.writeValueAsString(joinPoint.getArgs())));
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return joinPoint.proceed();
    }
}
