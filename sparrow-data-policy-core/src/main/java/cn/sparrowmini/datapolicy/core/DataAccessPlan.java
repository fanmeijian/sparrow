package cn.sparrowmini.datapolicy.core;
import java.util.*;
import cn.sparrowmini.security.api.*;
/** Per-request plan for trusted rows. SQL execution adapter is a separate implementation. */
public final class DataAccessPlan {
 private final FunctionDecision function;
 private final List<DataRule> rules;
 private final List<Condition> restrictions;
 public DataAccessPlan(FunctionDecision function, List<DataRule> rules) { this(function,rules,List.of()); }
 private DataAccessPlan(FunctionDecision function,List<DataRule> rules,List<Condition> restrictions) {
  this.function=Objects.requireNonNull(function); this.rules=List.copyOf(rules); this.restrictions=List.copyOf(restrictions);
 }
 public DataAccessPlan restrict(Condition condition) { var next=new ArrayList<>(restrictions); next.add(Objects.requireNonNull(condition)); return new DataAccessPlan(function,rules,next); }
 public boolean canAccess(Map<String,Object> row) { Objects.requireNonNull(row); return function.allowed() && restrictions.stream().allMatch(c -> c.matches(row)) && permitted(row,DataRule.Target.ROW,null); }
 public boolean canRead(Map<String,Object> row,String field) { return canAccess(row) && permitted(row,DataRule.Target.FIELD_READ,Objects.requireNonNull(field)); }
 public boolean canWrite(Map<String,Object> row,String field) { return canAccess(row) && permitted(row,DataRule.Target.FIELD_WRITE,Objects.requireNonNull(field)); }
 private boolean permitted(Map<String,Object> row,DataRule.Target target,String field) {
  boolean allow=false;
  for(DataRule r:rules) {
   if(!r.permission().equals(function.request()) || r.target()!=target || !r.subject().matches(function.actor()) || (target!=DataRule.Target.ROW && !r.fields().contains(field)) || !r.condition().matches(row)) continue;
   if(r.effect()==Effect.DENY) return false;
   if(r.subject().kind()!=Subject.Kind.ROLE || function.eligibleRoles().contains(r.subject().id())) allow=true;
  }
  return allow;
 }
}
