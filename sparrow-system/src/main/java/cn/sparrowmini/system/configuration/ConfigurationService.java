package cn.sparrowmini.system.configuration;

import jakarta.persistence.*;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.*;
import java.util.*;

/** Types and public exposure are registered by the application, never loaded from Java class names. */
public class ConfigurationService {
    public enum Kind { STRING, INTEGER, DECIMAL, BOOLEAN, JSON }
    public record Definition(String code,Kind kind,String defaultValue,boolean clientVisible) { }
    public record Value(String code,Kind kind,String value,Long version,boolean defaulted) { }
    @FunctionalInterface public interface Guard{void require(String operation,String code);}
    private final EntityManager em;private final String namespace;private final Guard guard;private final Map<String,Definition> definitions;
    private final ObjectMapper json=new ObjectMapper();
    public ConfigurationService(EntityManager em,String namespace,Guard guard,Collection<Definition> definitions){
        json.enable(com.fasterxml.jackson.databind.DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        json.getFactory().setStreamReadConstraints(com.fasterxml.jackson.core.StreamReadConstraints.builder().maxNestingDepth(64).build());
        this.em=Objects.requireNonNull(em);this.namespace=Objects.requireNonNull(namespace);this.guard=Objects.requireNonNull(guard);
        Map<String,Definition> map=new TreeMap<>();for(var d:definitions){if(d.code()==null||!d.code().matches("[a-zA-Z0-9_.-]{1,100}")||map.put(d.code(),d)!=null)throw new IllegalArgumentException("Invalid configuration registry");normalize(d.kind(),d.defaultValue());}this.definitions=Map.copyOf(map);
    }
    @Transactional(readOnly=true) public List<Value> list(){guard.require("read",null);return definitions.keySet().stream().sorted().map(this::current).toList();}
    @Transactional(readOnly=true) public Value get(String code){guard.require("read",code);return current(code);}
    @Transactional(readOnly=true) public Map<String,String> clientValues(){Map<String,String> result=new TreeMap<>();definitions.values().stream().filter(Definition::clientVisible).forEach(d->result.put(d.code(),current(d.code()).value()));return Map.copyOf(result);}
    @Transactional public Value save(String code,Long version,String value){
        guard.require("manage",code);transaction();var definition=definition(code);String normalized=normalize(definition.kind(),value);var found=find(code,true);ConfigurationItem item;
        if(found.isPresent()){item=found.get();if(version==null||version!=item.version)throw new OptimisticLockException("Stale configuration version");}
        else{if(version!=null)throw new OptimisticLockException("Missing configuration version");item=new ConfigurationItem();item.id=UUID.randomUUID().toString();item.namespace=namespace;item.code=code;item.value=normalized;em.persist(item);}
        item.value=normalized;em.flush();return new Value(code,definition.kind(),item.value,item.version,false);
    }
    @Transactional public Value reset(String code,long version){guard.require("manage",code);transaction();definition(code);var item=find(code,true).orElseThrow(()->new IllegalArgumentException("No override"));if(item.version!=version)throw new OptimisticLockException("Stale configuration version");em.remove(item);em.flush();return current(code);}
    private Value current(String code){var d=definition(code);return find(code,false).map(i->new Value(code,d.kind(),i.value,i.version,false)).orElseGet(()->new Value(code,d.kind(),normalize(d.kind(),d.defaultValue()),null,true));}
    private Definition definition(String code){var d=definitions.get(code);if(d==null)throw new IllegalArgumentException("Unknown configuration code");return d;}
    private Optional<ConfigurationItem> find(String code,boolean lock){var cb=em.getCriteriaBuilder();var q=cb.createQuery(ConfigurationItem.class);var r=q.from(ConfigurationItem.class);q.select(r).where(cb.equal(r.get("namespace"),namespace),cb.equal(r.get("code"),code));var query=em.createQuery(q);if(lock)query.setLockMode(LockModeType.PESSIMISTIC_WRITE);var found=query.getResultStream().findFirst();if(lock)found.ifPresent(em::refresh);return found;}
    private String normalize(Kind kind,String value){
        if(kind==null||value==null||value.length()>100000)throw new IllegalArgumentException("Invalid configuration value");
        try{return switch(kind){
            case STRING->value;
            case INTEGER->{if(value.length()>1000)throw new IllegalArgumentException("Integer limit");yield new BigInteger(value).toString();}
            case DECIMAL->{var number=new BigDecimal(value);if(Math.abs((long)number.scale())>1000||number.precision()>1000)throw new IllegalArgumentException("Decimal limit");yield number.stripTrailingZeros().toPlainString();}
            case BOOLEAN->{if(!Set.of("true","false").contains(value))throw new IllegalArgumentException("Expected boolean");yield value;}
            case JSON->{var parsed=json.readTree(value);if(parsed==null)throw new IllegalArgumentException("Empty JSON");yield json.writeValueAsString(parsed);}
        };}catch(java.io.IOException|NumberFormatException e){throw new IllegalArgumentException("Invalid typed value",e);}
    }
    private void transaction(){if(!em.isJoinedToTransaction())throw new IllegalStateException("Active transaction required");}
}
