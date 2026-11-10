package cn.sparrowmini.system.navigation;

import cn.sparrowmini.security.api.*;
import jakarta.persistence.*;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.*;
import java.math.BigDecimal;
import java.net.URI;

public class NavigationService {
    public record Item(String code,String parent,String title,String href,String sortOrder,boolean enabled,boolean publicVisible,PermissionRequest permission) { }
    public record VisibleItem(String code,String parent,String title,String href,String sortOrder) { }
    public record Snapshot(Long version,List<Item> items){public Snapshot{items=List.copyOf(items);}}
    @FunctionalInterface public interface Guard{void require(String operation);}
    private final EntityManager em;private final String namespace;private final Guard guard;private final FunctionPermissionEvaluator evaluator;private final Set<PermissionRequest> registered;private final ObjectMapper json=new ObjectMapper();
    public NavigationService(EntityManager em,String namespace,Guard guard,FunctionPermissionEvaluator evaluator,Set<PermissionRequest> registered){this.em=Objects.requireNonNull(em);this.namespace=Objects.requireNonNull(namespace);this.guard=Objects.requireNonNull(guard);this.evaluator=Objects.requireNonNull(evaluator);this.registered=Set.copyOf(registered);}
    @Transactional(readOnly=true) public Snapshot get(){guard.require("read");return snapshot();}
    @Transactional public Snapshot save(Long version,List<Item> items){
        guard.require("manage");if(!em.isJoinedToTransaction())throw new IllegalStateException("Active transaction required");validate(items);String document=encode(items);
        var entity=em.find(NavigationDocument.class,namespace,LockModeType.PESSIMISTIC_WRITE);
        if(entity==null){if(version!=null)throw new OptimisticLockException("Missing navigation version");entity=new NavigationDocument();entity.namespace=namespace;entity.document=document;em.persist(entity);}
        else{em.refresh(entity);if(version==null||version!=entity.version)throw new OptimisticLockException("Stale navigation version");}
        entity.document=document;em.flush();return new Snapshot(entity.version,items);
    }
    @Transactional(readOnly=true) public List<VisibleItem> visible(ActorContext actor){
        var snapshot=snapshot();Map<String,Item> map=new HashMap<>();snapshot.items().forEach(i->map.put(i.code(),i));Map<String,Boolean> memo=new HashMap<>();
        return snapshot.items().stream().filter(i->visible(actor,i,map,memo)).sorted(Comparator.comparing((Item i)->new BigDecimal(i.sortOrder())).thenComparing(Item::code))
            .map(i->new VisibleItem(i.code(),i.parent(),i.title(),i.href(),i.sortOrder())).toList();
    }
    private boolean visible(ActorContext actor,Item item,Map<String,Item> map,Map<String,Boolean> memo){
        var known=memo.get(item.code());if(known!=null)return known;
        boolean result=item.enabled()&&(item.publicVisible()||evaluator.evaluate(actor,item.permission()).allowed())&&(item.parent()==null||visible(actor,map.get(item.parent()),map,memo));memo.put(item.code(),result);return result;
    }
    private Snapshot snapshot(){var document=em.find(NavigationDocument.class,namespace);return document==null?new Snapshot(null,List.of()):new Snapshot(document.version,decode(document.document));}
    private void validate(List<Item> items){
        if(items.size()>1000)throw new IllegalArgumentException("Navigation capacity exceeded");Map<String,Item> map=new HashMap<>();
        for(var item:items){
            if(item.code()==null||!item.code().matches("[a-zA-Z0-9_.-]{1,100}")||map.put(item.code(),item)!=null)throw new IllegalArgumentException("Invalid navigation code");
            if(item.title()==null||item.title().isBlank()||item.title().length()>200)throw new IllegalArgumentException("Invalid navigation title");
            if(item.publicVisible()==(item.permission()!=null)||item.permission()!=null&&!registered.contains(item.permission()))throw new IllegalArgumentException("Invalid visibility binding");
            try{var uri=new URI(item.href());if(uri.isAbsolute()||uri.getRawAuthority()!=null||!item.href().startsWith("/")||item.href().startsWith("//"))throw new IllegalArgumentException("Only application-relative paths are allowed");}catch(java.net.URISyntaxException|NullPointerException e){throw new IllegalArgumentException("Invalid navigation target",e);}
            try{var order=new BigDecimal(item.sortOrder());if(order.precision()>28||Math.abs((long)order.scale())>8)throw new IllegalArgumentException("Sort precision limit");}catch(NumberFormatException|NullPointerException e){throw new IllegalArgumentException("Invalid sort value",e);}
        }
        for(var item:items){Set<String> seen=new HashSet<>();var current=item;while(current!=null){if(!seen.add(current.code())||seen.size()>64)throw new IllegalArgumentException("Navigation cycle or depth limit");if(current.parent()==null)break;current=map.get(current.parent());if(current==null)throw new IllegalArgumentException("Unknown navigation parent");}}
    }
    private String encode(List<Item> items){try{String value=json.writeValueAsString(items);if(value.length()>1000000)throw new IllegalArgumentException("Navigation size limit");return value;}catch(java.io.IOException e){throw new IllegalArgumentException("Invalid navigation",e);}}
    private List<Item> decode(String text){try{var items=json.readValue(text,new TypeReference<List<Item>>(){});validate(items);return List.copyOf(items);}catch(java.io.IOException e){throw new IllegalStateException("Invalid stored navigation",e);}}
}
