package cn.sparrowmini.datapolicy.persistence.jpa;

import cn.sparrowmini.datapolicy.core.*;
import cn.sparrowmini.security.api.*;
import jakarta.persistence.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

public class JpaPolicyStore {
    @FunctionalInterface public interface Guard {void require(String namespace,String operation,PermissionRequest permission);}
    public record Snapshot(long version,long activeRevision,List<DataRule> rules){public Snapshot{rules=List.copyOf(rules);}}
    private final EntityManager em;
    private final String namespace;
    private final Guard guard;
    private final Map<PermissionRequest,JpaResource<?>> schema;
    private final PolicyJsonCodec codec=new PolicyJsonCodec();
    public JpaPolicyStore(EntityManager em,String namespace,Guard guard,Map<PermissionRequest,JpaResource<?>> schema){
        this.em=Objects.requireNonNull(em);this.namespace=Objects.requireNonNull(namespace);this.guard=Objects.requireNonNull(guard);
        this.schema=Map.copyOf(schema);this.schema.forEach((request,resource)->{resource.validate(em);if(!request.resource().equals(resource.code()))throw new IllegalArgumentException("Resource schema mismatch");});
    }
    @Transactional public Snapshot saveDraft(PermissionRequest request,Long expectedVersion,List<DataRule> rules){
        guard.require(namespace,"update",request);transaction();validate(request,rules);var found=find(request,true);
        PolicyDocument doc;
        if(found.isEmpty()){
            if(expectedVersion!=null)throw new OptimisticLockException("Missing policy version");
            doc=new PolicyDocument();doc.id=UUID.randomUUID().toString();doc.namespace=namespace;doc.resource=request.resource();doc.scope=request.scope();doc.draftJson=codec.encode(rules);em.persist(doc);
        }else{doc=found.get();if(expectedVersion==null || doc.version!=expectedVersion)throw new OptimisticLockException("Stale policy version");doc.draftJson=codec.encode(rules);}
        em.flush();return new Snapshot(doc.version,doc.activeRevision,rules);
    }
    @Transactional public Snapshot publish(PermissionRequest request,long expectedVersion){
        guard.require(namespace,"publish",request);transaction();var doc=find(request,true).orElseThrow();
        if(doc.version!=expectedVersion)throw new OptimisticLockException("Stale policy version");
        var rules=codec.decode(doc.draftJson);validate(request,rules);doc.activeJson=doc.draftJson;doc.activeRevision++;em.flush();
        return new Snapshot(doc.version,doc.activeRevision,rules);
    }
    @Transactional public void retire(PermissionRequest request,long expectedVersion){
        guard.require(namespace,"retire",request);transaction();var doc=find(request,true).orElseThrow();
        if(doc.version!=expectedVersion)throw new OptimisticLockException("Stale policy version");doc.activeJson=null;doc.activeRevision++;em.flush();
    }
    /** Internal evaluation port: unknown or unpublished permissions return no data grants. */
    @Transactional(readOnly=true) public Snapshot active(PermissionRequest request){
        var found=find(request,false);
        if(found.isEmpty())return new Snapshot(0,0,List.of());
        var doc=found.get();return new Snapshot(doc.version,doc.activeRevision,doc.activeJson==null?List.of():codec.decode(doc.activeJson));
    }
    @Transactional(readOnly=true) public Snapshot draft(PermissionRequest request){
        guard.require(namespace,"read",request);var doc=find(request,false).orElseThrow();return new Snapshot(doc.version,doc.activeRevision,codec.decode(doc.draftJson));
    }
    private Optional<PolicyDocument> find(PermissionRequest request,boolean lock){
        var cb=em.getCriteriaBuilder();var q=cb.createQuery(PolicyDocument.class);var root=q.from(PolicyDocument.class);
        q.select(root).where(cb.equal(root.get("namespace"),namespace),cb.equal(root.get("resource"),request.resource()),cb.equal(root.get("scope"),request.scope()));
        var query=em.createQuery(q);if(lock)query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        var result=query.getResultStream().findFirst();if(lock)result.ifPresent(d->em.refresh(d,LockModeType.PESSIMISTIC_WRITE));return result;
    }
    private void validate(PermissionRequest request,List<DataRule> rules){
        var resource=schema.get(request);if(resource==null)throw new IllegalArgumentException("Unknown resource/scope");
        for(var rule:rules)if(!request.equals(rule.permission()) || !resource.fields().containsAll(rule.fields()) || !resource.fields().containsAll(Condition.fields(rule.condition())))throw new IllegalArgumentException("Invalid policy field or permission");
        rules.forEach(rule->validateCondition(resource,rule.condition()));
        codec.encode(rules);
    }
    private <T> void validateCondition(JpaResource<T> resource,Condition condition){
        var cb=em.getCriteriaBuilder();var query=cb.createQuery(resource.entityType());var root=query.from(resource.entityType());
        JpaPredicateCompiler.compile(resource,root,cb,condition,0);
    }
    private void transaction(){if(!em.isJoinedToTransaction())throw new IllegalStateException("Active transaction required");}
}
