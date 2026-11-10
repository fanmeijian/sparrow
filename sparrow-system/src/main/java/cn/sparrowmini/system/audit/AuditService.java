package cn.sparrowmini.system.audit;

import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.*;
import java.util.*;

/** Records stable IDs and outcomes only; credentials and request/response bodies are never accepted. */
public class AuditService {
    public record Event(String id,String subject,String operation,String target,long occurredAt,String outcome,String requestId,Long policyRevision,String reason) { }
    @FunctionalInterface public interface Guard {void requireRead(String namespace);}
    private final EntityManager em;private final String namespace;private final Guard guard;
    public AuditService(EntityManager em,String namespace,Guard guard){this.em=Objects.requireNonNull(em);this.namespace=Objects.requireNonNull(namespace);this.guard=Objects.requireNonNull(guard);}
    @Transactional(propagation=Propagation.MANDATORY)
    public void record(String subject,String operation,String target,String outcome,String requestId,Long policyRevision,String reason){
        if(!em.isJoinedToTransaction())throw new IllegalStateException("Audit requires transaction");
        check(subject,300);check(operation,12);check(target,500);check(requestId,36);check(reason,100);
        if(!Set.of("SUCCEEDED","DENIED","CONFLICT","INVALID","FAILED").contains(outcome))throw new IllegalArgumentException("Invalid outcome");
        var event=new AuditEvent();event.id=UUID.randomUUID().toString();event.namespace=namespace;event.subject=subject;event.operation=operation;event.target=target;event.occurredAt=System.currentTimeMillis();event.outcome=outcome;event.requestId=requestId;event.policyRevision=policyRevision;event.reason=reason;em.persist(event);em.flush();
    }
    @Transactional(readOnly=true)
    public List<Event> list(int offset,int limit){
        guard.requireRead(namespace);if(offset<0||limit<1||limit>500)throw new IllegalArgumentException("Invalid pagination");
        var cb=em.getCriteriaBuilder();var q=cb.createQuery(AuditEvent.class);var root=q.from(AuditEvent.class);
        q.select(root).where(cb.equal(root.get("namespace"),namespace)).orderBy(cb.desc(root.get("occurredAt")),cb.asc(root.get("id")));
        return em.createQuery(q).setFirstResult(offset).setMaxResults(limit).getResultList().stream().map(e->new Event(e.id,e.subject,e.operation,e.target,e.occurredAt,e.outcome,e.requestId,e.policyRevision,e.reason)).toList();
    }
    private void check(String value,int max){if(value==null||value.isBlank()||value.length()>max)throw new IllegalArgumentException("Invalid audit metadata");}
}
