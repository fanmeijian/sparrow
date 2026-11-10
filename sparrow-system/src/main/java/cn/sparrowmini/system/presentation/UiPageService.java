package cn.sparrowmini.system.presentation;

import cn.sparrowmini.security.api.ActorContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.function.Consumer;

public class UiPageService {
    public record Definition(List<UiCapabilityService.Element> elements,List<UiCapabilityService.Policy> policies){public Definition{elements=List.copyOf(elements);policies=List.copyOf(policies);}}
    public record Snapshot(Long version,Definition definition) { }
    @FunctionalInterface public interface Guard{void require(String operation,String page);}
    private final EntityManager em;private final String namespace;private final Guard guard;
    private final Consumer<UiCapabilityService.Element> validator;private final UiCapabilityService.Resolver resolver;private final ObjectMapper json=new ObjectMapper();
    public UiPageService(EntityManager em,String namespace,Guard guard,Consumer<UiCapabilityService.Element> validator,UiCapabilityService.Resolver resolver){this.em=Objects.requireNonNull(em);this.namespace=Objects.requireNonNull(namespace);this.guard=Objects.requireNonNull(guard);this.validator=Objects.requireNonNull(validator);this.resolver=Objects.requireNonNull(resolver);}
    @Transactional(readOnly=true) public List<String> pages(){
        guard.require("read",null);var cb=em.getCriteriaBuilder();var q=cb.createQuery(String.class);var r=q.from(UiPage.class);q.select(r.get("code")).where(cb.equal(r.get("namespace"),namespace)).orderBy(cb.asc(r.get("code")));return em.createQuery(q).setMaxResults(1000).getResultList();
    }
    @Transactional(readOnly=true) public Snapshot get(String page){guard.require("read",page);return find(page,false).map(p->new Snapshot(p.version,decode(p.document))).orElse(new Snapshot(null,new Definition(List.of(),List.of())));}
    @Transactional public Snapshot save(String page,Long version,Definition definition){
        guard.require("manage",page);if(!em.isJoinedToTransaction())throw new IllegalStateException("Active transaction required");
        if(page==null||!page.matches("[a-zA-Z0-9_.-]{1,100}"))throw new IllegalArgumentException("Invalid page code");
        validate(definition);String document=encode(definition);var found=find(page,true);UiPage entity;
        if(found.isPresent()){entity=found.get();if(version==null||version!=entity.version)throw new OptimisticLockException("Stale page version");}
        else{if(version!=null)throw new OptimisticLockException("Missing page version");entity=new UiPage();entity.id=UUID.randomUUID().toString();entity.namespace=namespace;entity.code=page;entity.document=document;em.persist(entity);}
        entity.document=document;em.flush();return new Snapshot(entity.version,definition);
    }
    /** No management privilege is needed to evaluate the caller's own UI capabilities. */
    @Transactional(readOnly=true) public Map<String,UiCapabilityService.Decision> capabilities(ActorContext actor,String page,Collection<String> codes,String record){
        if(codes.size()>200)throw new IllegalArgumentException("Element batch limit");var stored=find(page,false);
        if(stored.isEmpty()){Map<String,UiCapabilityService.Decision> denied=new HashMap<>();codes.forEach(code->denied.put(code,new UiCapabilityService.Decision(false,false,false,"NOT_AVAILABLE")));return Map.copyOf(denied);}
        var definition=decode(stored.get().document);return new UiCapabilityService(definition.elements(),definition.policies(),resolver).batch(actor,codes,record);
    }
    private void validate(Definition definition){
        if(definition.elements().size()>5000||definition.policies().size()>10000)throw new IllegalArgumentException("Page capacity exceeded");
        definition.elements().forEach(validator);new UiCapabilityService(definition.elements(),definition.policies(),resolver);
    }
    private Optional<UiPage> find(String code,boolean lock){var cb=em.getCriteriaBuilder();var q=cb.createQuery(UiPage.class);var r=q.from(UiPage.class);q.select(r).where(cb.equal(r.get("namespace"),namespace),cb.equal(r.get("code"),code));var query=em.createQuery(q);if(lock)query.setLockMode(LockModeType.PESSIMISTIC_WRITE);var result=query.getResultStream().findFirst();if(lock)result.ifPresent(em::refresh);return result;}
    private String encode(Definition value){try{String text=json.writeValueAsString(value);if(text.length()>1000000)throw new IllegalArgumentException("Page size limit");return text;}catch(java.io.IOException e){throw new IllegalArgumentException("Invalid page",e);}}
    private Definition decode(String text){try{return json.readValue(text,Definition.class);}catch(java.io.IOException e){throw new IllegalStateException("Invalid stored page",e);}}
}
