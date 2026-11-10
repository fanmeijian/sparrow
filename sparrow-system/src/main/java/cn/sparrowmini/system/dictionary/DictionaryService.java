package cn.sparrowmini.system.dictionary;

import jakarta.persistence.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

public class DictionaryService {
    public record Definition(String id,String code,String name,long version) { }
    public record Entry(String id,String code,String label,int order,boolean enabled,long version) { }
    @FunctionalInterface public interface Guard{void require(String namespace,String scope,String dictionaryCode);}
    @FunctionalInterface public interface ReferenceCheck{boolean isUnreferenced(String dictionaryCode,String entryCode);}
    private final EntityManager em;private final String namespace;private final Guard guard;private final ReferenceCheck references;
    public DictionaryService(EntityManager em,String namespace,Guard guard,ReferenceCheck references){this.em=Objects.requireNonNull(em);this.namespace=Objects.requireNonNull(namespace);this.guard=Objects.requireNonNull(guard);this.references=Objects.requireNonNull(references);}
    @Transactional public Definition create(String code,String name){
        guard.require(namespace,"create",code);transaction();validCode(code);validLabel(name);
        var d=new DictionaryDefinition();d.id=UUID.randomUUID().toString();d.namespace=namespace;d.code=code;d.name=name;em.persist(d);em.flush();return definition(d);
    }
    @Transactional(readOnly=true) public List<Definition> definitions(int offset,int limit){
        guard.require(namespace,"read",null);bounds(offset,limit);var cb=em.getCriteriaBuilder();var q=cb.createQuery(DictionaryDefinition.class);var root=q.from(DictionaryDefinition.class);
        q.select(root).where(cb.equal(root.get("namespace"),namespace)).orderBy(cb.asc(root.get("code")));
        return em.createQuery(q).setFirstResult(offset).setMaxResults(limit).getResultList().stream().map(this::definition).toList();
    }
    @Transactional public Entry add(String dictionary,String code,String label,int order){
        guard.require(namespace,"update",dictionary);transaction();validCode(code);validLabel(label);var d=find(dictionary,true);
        var e=new DictionaryEntry();e.id=UUID.randomUUID().toString();e.dictionary=d;e.code=code;e.label=label;e.sortOrder=order;e.enabled=true;em.persist(e);em.flush();return entry(e);
    }
    @Transactional public Entry update(String dictionary,String id,long expectedVersion,String label,int order,boolean enabled){
        guard.require(namespace,"update",dictionary);transaction();validLabel(label);find(dictionary,true);var e=findEntry(dictionary,id);
        em.refresh(e,LockModeType.PESSIMISTIC_WRITE);if(e.version!=expectedVersion)throw new OptimisticLockException("Stale entry version");
        e.label=label;e.sortOrder=order;e.enabled=enabled;em.flush();return entry(e);
    }
    @Transactional(readOnly=true) public List<Entry> entries(String dictionary,boolean selectableOnly,int offset,int limit){
        guard.require(namespace,"read",dictionary);bounds(offset,limit);var cb=em.getCriteriaBuilder();var q=cb.createQuery(DictionaryEntry.class);var root=q.from(DictionaryEntry.class);
        var condition=cb.and(cb.equal(root.get("dictionary").get("namespace"),namespace),cb.equal(root.get("dictionary").get("code"),dictionary));
        if(selectableOnly)condition=cb.and(condition,cb.isTrue(root.get("enabled")));
        q.select(root).where(condition).orderBy(cb.asc(root.get("sortOrder")),cb.asc(root.get("code")));
        return em.createQuery(q).setFirstResult(offset).setMaxResults(limit).getResultList().stream().map(this::entry).toList();
    }
    @Transactional public void deleteEntry(String dictionary,String id,long expectedVersion){
        guard.require(namespace,"delete",dictionary);transaction();find(dictionary,true);var e=findEntry(dictionary,id);em.refresh(e,LockModeType.PESSIMISTIC_WRITE);
        if(e.version!=expectedVersion)throw new OptimisticLockException("Stale entry version");
        if(!references.isUnreferenced(dictionary,e.code))throw new IllegalStateException("Entry is referenced or references cannot be verified");
        em.remove(e);em.flush();
    }
    private DictionaryDefinition find(String code,boolean lock){
        var cb=em.getCriteriaBuilder();var q=cb.createQuery(DictionaryDefinition.class);var root=q.from(DictionaryDefinition.class);
        q.select(root).where(cb.equal(root.get("namespace"),namespace),cb.equal(root.get("code"),code));var query=em.createQuery(q);if(lock)query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        return query.getResultStream().findFirst().orElseThrow(()->new IllegalArgumentException("Unknown dictionary"));
    }
    private DictionaryEntry findEntry(String dictionary,String id){
        var cb=em.getCriteriaBuilder();var q=cb.createQuery(DictionaryEntry.class);var root=q.from(DictionaryEntry.class);
        q.select(root).where(cb.equal(root.get("id"),id),cb.equal(root.get("dictionary").get("namespace"),namespace),cb.equal(root.get("dictionary").get("code"),dictionary));
        return em.createQuery(q).getResultStream().findFirst().orElseThrow(()->new IllegalArgumentException("Unknown entry"));
    }
    private Definition definition(DictionaryDefinition d){return new Definition(d.id,d.code,d.name,d.version);}
    private Entry entry(DictionaryEntry e){return new Entry(e.id,e.code,e.label,e.sortOrder,e.enabled,e.version);}
    private void transaction(){if(!em.isJoinedToTransaction())throw new IllegalStateException("Active transaction required");}
    private static void validCode(String s){if(s==null || !s.matches("[a-zA-Z0-9_.-]{1,100}"))throw new IllegalArgumentException("Invalid code");}
    private static void validLabel(String s){if(s==null || s.isBlank() || s.length()>200)throw new IllegalArgumentException("Invalid label");}
    private static void bounds(int offset,int limit){if(offset<0 || limit<1 || limit>500)throw new IllegalArgumentException("Invalid pagination");}
}
