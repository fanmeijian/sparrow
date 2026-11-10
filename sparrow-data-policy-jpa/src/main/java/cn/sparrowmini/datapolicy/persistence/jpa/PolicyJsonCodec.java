package cn.sparrowmini.datapolicy.persistence.jpa;

import cn.sparrowmini.datapolicy.core.*;
import cn.sparrowmini.security.api.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import java.util.*;

/** Explicit JSON schema: no class names, reflection-based type loading or executable expressions. */
public final class PolicyJsonCodec {
    private final ObjectMapper mapper=new ObjectMapper();
    public String encode(List<DataRule> rules){
        if(rules.size()>1000)throw new IllegalArgumentException("Policy rule limit");
        var array=mapper.createArrayNode();
        for(var r:rules){var n=array.addObject();n.put("kind",r.subject().kind().name());n.put("subject",r.subject().id());
            n.put("resource",r.permission().resource());n.put("scope",r.permission().scope());n.put("effect",r.effect().name());n.put("target",r.target().name());
            var fields=n.putArray("fields");new TreeSet<>(r.fields()).forEach(fields::add);n.set("condition",encodeCondition(r.condition(),0));}
        var json=array.toString();if(json.length()>1_000_000)throw new IllegalArgumentException("Policy size limit");return json;
    }
    public List<DataRule> decode(String json){
        try{
            if(json.length()>1_000_000)throw new IllegalArgumentException("Policy size limit");
            var array=mapper.readTree(json);if(!array.isArray() || array.size()>1000)throw new IllegalArgumentException("Invalid policy list");
            List<DataRule> result=new ArrayList<>();
            for(var n:array){Set<String> fields=new HashSet<>();for(var f:n.required("fields"))fields.add(f.asText());
                result.add(new DataRule(new Subject(Subject.Kind.valueOf(text(n,"kind")),text(n,"subject")),new PermissionRequest(text(n,"resource"),text(n,"scope")),Effect.valueOf(text(n,"effect")),decodeCondition(n.required("condition"),0),DataRule.Target.valueOf(text(n,"target")),fields));}
            return List.copyOf(result);
        }catch(java.io.IOException | IllegalArgumentException e){throw new IllegalArgumentException("Invalid policy document",e);}
    }
    private ObjectNode encodeCondition(Condition c,int depth){
        if(depth>64)throw new IllegalArgumentException("Condition depth limit");var n=mapper.createObjectNode();
        if(c instanceof Condition.All)n.put("op","all");else if(c instanceof Condition.None)n.put("op","none");
        else if(c instanceof Condition.Equal e){n.put("op","eq");n.put("field",e.field());n.set("value",literal(e.value()));}
        else if(c instanceof Condition.In e){n.put("op","in");n.put("field",e.field());var a=n.putArray("values");e.values().forEach(v->a.add(literal(v)));}
        else if(c instanceof Condition.Not e){n.put("op","not");n.set("term",encodeCondition(e.term(),depth+1));}
        else{n.put("op",c instanceof Condition.And?"and":"or");var a=n.putArray("terms");var children=c instanceof Condition.And e?e.terms():((Condition.Or)c).terms();children.forEach(t->a.add(encodeCondition(t,depth+1)));}
        return n;
    }
    private Condition decodeCondition(JsonNode n,int depth){
        if(depth>64)throw new IllegalArgumentException("Condition depth limit");
        return switch(text(n,"op")){
            case "all"->new Condition.All();case "none"->new Condition.None();
            case "eq"->new Condition.Equal(text(n,"field"),value(n.required("value")));
            case "in"->{var values=new ArrayList<Object>();for(var v:n.required("values"))values.add(value(v));yield new Condition.In(text(n,"field"),values);}
            case "not"->new Condition.Not(decodeCondition(n.required("term"),depth+1));
            case "and","or"->{var terms=new ArrayList<Condition>();for(var t:n.required("terms"))terms.add(decodeCondition(t,depth+1));yield text(n,"op").equals("and")?new Condition.And(terms):new Condition.Or(terms);}
            default->throw new IllegalArgumentException("Unsupported condition operator");
        };
    }
    private ObjectNode literal(Object value){
        var n=mapper.createObjectNode();String type=value==null?"null":value instanceof Long?"long":value instanceof Integer?"int":value instanceof Boolean?"boolean":value instanceof java.math.BigDecimal?"decimal":"string";
        n.put("type",type);if(value!=null)n.put("value",value.toString());return n;
    }
    private Object value(JsonNode n){return switch(text(n,"type")){
        case "null"->null;case "long"->Long.valueOf(text(n,"value"));case "int"->Integer.valueOf(text(n,"value"));
        case "decimal"->new java.math.BigDecimal(text(n,"value"));case "string"->text(n,"value");
        case "boolean"->{var s=text(n,"value");if(!Set.of("true","false").contains(s))throw new IllegalArgumentException("Invalid boolean");yield Boolean.valueOf(s);}
        default->throw new IllegalArgumentException("Unsupported literal type");};}
    private String text(JsonNode n,String field){var v=n.required(field);if(!v.isTextual())throw new IllegalArgumentException("Expected text");return v.textValue();}
}
