package cn.sparrowmini.system.presentation;

import cn.sparrowmini.security.api.*;
import java.util.*;

/** Presentation constraints are always intersected with authoritative business capabilities. */
public final class UiCapabilityService {
    public enum Mode { DERIVED, RESTRICTED }
    public enum Operation { VIEW, INTERACT }
    public enum Kind { BUTTON, FIELD, PANEL }
    public record Element(String code,String parent,Kind kind,boolean publicVisible,PermissionRequest view,
                          PermissionRequest action,String field,Mode mode,boolean hideWhenDenied) {
        public Element {
            if(code==null || code.isBlank())throw new IllegalArgumentException("Element code required");
            Objects.requireNonNull(kind);Objects.requireNonNull(mode);
            if(publicVisible==(view!=null))throw new IllegalArgumentException("Choose public visibility or a view permission");
            if(kind==Kind.FIELD && (field==null || field.isBlank() || view==null))throw new IllegalArgumentException("Field read binding required");
            if(kind==Kind.BUTTON && action==null)throw new IllegalArgumentException("Button action required");
        }
    }
    public record Policy(String element,Subject subject,Operation operation,Effect effect) {
        public Policy {Objects.requireNonNull(element);Objects.requireNonNull(subject);Objects.requireNonNull(operation);Objects.requireNonNull(effect);}
    }
    public record Decision(boolean visible,boolean enabled,boolean readOnly,String reason) { }
    @FunctionalInterface public interface Resolver {
        boolean allowed(ActorContext actor,PermissionRequest permission,String recordId,String field,Operation operation);
    }
    private final Map<String,Element> elements;
    private final List<Policy> policies;
    private final Resolver resolver;
    public UiCapabilityService(Collection<Element> elements,List<Policy> policies,Resolver resolver){
        var map=new HashMap<String,Element>();for(var e:elements)if(map.put(e.code(),e)!=null)throw new IllegalArgumentException("Duplicate element");
        this.elements=Map.copyOf(map);this.policies=List.copyOf(policies);this.resolver=Objects.requireNonNull(resolver);
        for(var e:elements){Set<String> path=new HashSet<>();Element n=e;while(n!=null){if(path.size()>=64)throw new IllegalArgumentException("Element depth limit");if(!path.add(n.code()))throw new IllegalArgumentException("Element parent cycle");if(n.parent()==null)break;n=map.get(n.parent());if(n==null)throw new IllegalArgumentException("Unknown parent");}}
        for(var p:policies)if(!map.containsKey(p.element()))throw new IllegalArgumentException("Unknown element policy");
    }
    public Decision decide(ActorContext actor,String code,String recordId){
        Objects.requireNonNull(actor);var e=elements.get(code);if(e==null)throw new IllegalArgumentException("Unknown element");
        Decision parent=e.parent()==null?new Decision(true,true,false,"ALLOWED"):decide(actor,e.parent(),recordId);
        if(!parent.visible())return hidden();
        boolean visible=(e.publicVisible() || resolver.allowed(actor,e.view(),recordId,e.field(),Operation.VIEW)) && presentation(actor,e,Operation.VIEW);
        if(!visible)return hidden();
        boolean permitted=(e.action()==null ? e.kind()==Kind.PANEL : resolver.allowed(actor,e.action(),recordId,e.field(),Operation.INTERACT)) && presentation(actor,e,Operation.INTERACT) && parent.enabled() && !parent.readOnly();
        if(!permitted && e.hideWhenDenied())return hidden();
        return new Decision(true,permitted,e.kind()==Kind.FIELD && !permitted,permitted?"ALLOWED":"NOT_AVAILABLE");
    }
    public Map<String,Decision> batch(ActorContext actor,Collection<String> codes,String recordId){
        if(codes.size()>200)throw new IllegalArgumentException("Element batch limit");var result=new LinkedHashMap<String,Decision>();
        for(String code:codes)result.put(code,decide(actor,code,recordId));return Collections.unmodifiableMap(result);
    }
    private boolean presentation(ActorContext actor,Element element,Operation operation){
        if(element.mode()==Mode.DERIVED)return true;boolean allow=false;
        for(var policy:policies)if(policy.element().equals(element.code()) && policy.operation()==operation && policy.subject().matches(actor)){
            if(policy.effect()==Effect.DENY)return false;allow=true;
        }
        return allow;
    }
    private static Decision hidden(){return new Decision(false,false,false,"NOT_AVAILABLE");}
}
