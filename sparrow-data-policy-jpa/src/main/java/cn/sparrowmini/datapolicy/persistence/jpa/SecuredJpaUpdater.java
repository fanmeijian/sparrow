package cn.sparrowmini.datapolicy.persistence.jpa;

import cn.sparrowmini.data.jpa.query.JpaPredicateCompiler;

import cn.sparrowmini.data.jpa.query.JpaResource;

import cn.sparrowmini.core.query.Condition;

import cn.sparrowmini.datapolicy.core.*;
import jakarta.persistence.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.BiConsumer;

/** Transaction-scoped patch executor. Never binds untrusted input directly onto a managed entity. */
public final class SecuredJpaUpdater<T> {
    private final EntityManager em;
    private final JpaResource<T> resource;
    private final String versionField;
    private final Set<String> writable;
    private final BiConsumer<Map<String,Object>,Map<String,Object>> transitionGuard;

    public SecuredJpaUpdater(EntityManager em, JpaResource<T> resource, String versionField,
                             Set<String> writable, BiConsumer<Map<String,Object>,Map<String,Object>> transitionGuard) {
        this.em=Objects.requireNonNull(em); this.resource=Objects.requireNonNull(resource);
        this.versionField=Objects.requireNonNull(versionField); this.writable=Set.copyOf(writable);
        this.transitionGuard=Objects.requireNonNull(transitionGuard); resource.validate(em);
        var version=em.getMetamodel().entity(resource.entityType()).getSingularAttribute(versionField);
        if (!version.isVersion()) throw new IllegalArgumentException("Version attribute required");
        writable.forEach(resource::requireField);
        if(writable.contains(resource.id()) || writable.contains(versionField)) throw new IllegalArgumentException("Immutable field");
    }
    public void patch(DataAccessPlan plan, Object id, Object expectedVersion, Map<String,Object> patch) {
        if(!em.isJoinedToTransaction()) throw new IllegalStateException("Active transaction required");
        if(!plan.permission().resource().equals(resource.code()) || !plan.permission().scope().equals("update"))
            throw new SecurityException("Update permission required");
        Objects.requireNonNull(expectedVersion); Objects.requireNonNull(id); Objects.requireNonNull(patch);
        if(!writable.containsAll(patch.keySet())) throw new SecurityException("Field is not writable");
        var cb=em.getCriteriaBuilder(); var query=cb.createQuery(resource.entityType()); var root=query.from(resource.entityType());
        var condition=new Condition.And(List.of(plan.rowCondition(),new Condition.Equal(resource.id(),id)));
        query.select(root).where(JpaPredicateCompiler.specification(resource,condition).toPredicate(root,query,cb));
        for(String f:patch.keySet()) JpaPredicateCompiler.compile(resource,root,cb,plan.fieldCondition(f,DataRule.Target.FIELD_WRITE),0);
        var candidates=em.createQuery(query).setLockMode(LockModeType.PESSIMISTIC_WRITE).getResultList();
        if(candidates.size()!=1) throw new SecurityException("Record not available");
        T entity=candidates.get(0);
        if(!Objects.equals(read(entity,versionField),expectedVersion)) throw new OptimisticLockException("Stale version");
        Map<String,Object> before=new HashMap<>();resource.fields().forEach(f->before.put(f,read(entity,f)));
        Map<String,Object> after=new HashMap<>(before);
        for(var change:patch.entrySet()) {
            validateValue(change.getKey(),change.getValue());
            if(!plan.canWrite(before,change.getKey())) throw new SecurityException("Field write denied");
            after.put(change.getKey(),change.getValue());
        }
        if(!plan.canAccess(after)) throw new SecurityException("Target state outside data scope");
        for(String f:patch.keySet()) if(!plan.canWrite(after,f)) throw new SecurityException("Target field write denied");
        transitionGuard.accept(Collections.unmodifiableMap(before),Collections.unmodifiableMap(after));
        // All policy and business checks precede the first mutation.
        for(var change:patch.entrySet()) write(entity,change.getKey(),change.getValue());
        em.flush();
    }
    private Member member(String field) {return em.getMetamodel().entity(resource.entityType()).getAttribute(field).getJavaMember();}
    private Object read(T entity,String field) {
        try {
            Member member=member(field);
            if(member instanceof Field f){f.setAccessible(true);return f.get(entity);}
            return ((Method)member).invoke(entity);
        } catch(ReflectiveOperationException e){throw new IllegalStateException("Cannot read mapped field",e);}
    }
    private void write(T entity,String field,Object value) {
        try {
            Member member=member(field);
            if(member instanceof Field f){f.setAccessible(true);f.set(entity,value);}
            else {
                String name="set"+Character.toUpperCase(field.charAt(0))+field.substring(1);
                entity.getClass().getMethod(name,((Method)member).getReturnType()).invoke(entity,value);
            }
        } catch(ReflectiveOperationException e){throw new IllegalStateException("Cannot write mapped field",e);}
    }
    private void validateValue(String field,Object value){
        Class<?> type=em.getMetamodel().entity(resource.entityType()).getAttribute(field).getJavaType();
        if(value==null && type.isPrimitive())throw new IllegalArgumentException("Null primitive");
        if(type==int.class)type=Integer.class;if(type==long.class)type=Long.class;if(type==boolean.class)type=Boolean.class;
        if(value!=null && !type.isInstance(value))throw new IllegalArgumentException("Incorrect field type");
    }
}
