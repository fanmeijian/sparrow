package cn.sparrowmini.datapolicy.core;

import cn.sparrowmini.core.query.Condition;

import cn.sparrowmini.security.api.*;
import cn.sparrowmini.rbac.core.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.*;
import java.util.stream.IntStream;
import static org.junit.jupiter.api.Assertions.*;

class AuthorizationTest {
 static final PermissionRequest READ=new PermissionRequest("sales.order","read");
 static final ActorContext ACTOR=new ActorContext("tenant:user",Set.of("east","south"),Set.of("team"));
 static final Map<String,Object> EAST=Map.of("region","east","cost",100);
 static final Map<String,Object> SOUTH=Map.of("region","south","cost",200);
 static IntStream combinations(){return IntStream.range(0,81);}
 static DataRule rule(String role,Effect effect,Condition condition,DataRule.Target target,String... fields){
  return new DataRule(new Subject(Subject.Kind.ROLE,role),READ,effect,condition,target,Set.of(fields));
 }
 static FunctionDecision decision(List<RoleGrant> grants){return new RolePermissionEvaluator(Set.of(READ),grants).evaluate(ACTOR,READ);}
 static FunctionDecision both(){return decision(List.of(new RoleGrant("east",READ,Effect.ALLOW),new RoleGrant("south",READ,Effect.ALLOW)));}
 @ParameterizedTest @MethodSource("combinations")
 void allRoleAndRowGrantCombinations(int n){
  // Each role's function grant and row grant independently absent/allow/deny: 3^4 cases.
  int a=n%3,b=n/3%3,c=n/9%3,d=n/27%3;
  List<RoleGrant> functions=new ArrayList<>(); List<DataRule> rows=new ArrayList<>();
  String[] names={"east","south"}; int[] f={a,b},r={c,d};
  for(int i=0;i<2;i++){
   if(f[i]>0)functions.add(new RoleGrant(names[i],READ,f[i]==1?Effect.ALLOW:Effect.DENY));
   if(r[i]>0)rows.add(rule(names[i],r[i]==1?Effect.ALLOW:Effect.DENY,new Condition.All(),DataRule.Target.ROW));
  }
  boolean expected=a!=2 && b!=2 && c!=2 && d!=2 && ((a==1&&c==1)||(b==1&&d==1));
  assertEquals(expected,new DataAccessPlan(decision(functions),rows).canAccess(EAST),"combination="+n);
  Collections.reverse(functions); Collections.reverse(rows);
  assertEquals(expected,new DataAccessPlan(decision(functions),rows).canAccess(EAST));
 }
 @Test void fieldsKeepTheirOwnRowConditions(){
  var p=new DataAccessPlan(both(),List.of(
   rule("east",Effect.ALLOW,new Condition.Equal("region","east"),DataRule.Target.ROW),
   rule("south",Effect.ALLOW,new Condition.Equal("region","south"),DataRule.Target.ROW),
   rule("east",Effect.ALLOW,new Condition.Equal("region","east"),DataRule.Target.FIELD_READ,"cost"),
   rule("south",Effect.ALLOW,new Condition.Equal("region","south"),DataRule.Target.FIELD_READ,"customer")));
  assertTrue(p.canRead(EAST,"cost")); assertFalse(p.canRead(SOUTH,"cost"));
  assertTrue(p.canRead(SOUTH,"customer")); assertFalse(p.canRead(EAST,"customer"));
  assertFalse(p.canWrite(EAST,"cost")); assertFalse(p.canRead(EAST,"newField"));
 }
 @Test void additionalRestrictionCannotGrant(){
  var allow=new DataAccessPlan(both(),List.of(rule("east",Effect.ALLOW,new Condition.All(),DataRule.Target.ROW)));
  var limited=allow.restrict(new Condition.Equal("region","east")).restrict(new Condition.All());
  assertTrue(limited.canAccess(EAST)); assertFalse(limited.canAccess(SOUTH)); assertTrue(allow.canAccess(SOUTH));
  assertFalse(new DataAccessPlan(both(),List.of()).restrict(new Condition.All()).canAccess(EAST));
 }
 @Test void userAndGroupPoliciesStillRequireFunctionPermission(){
  for(var subject:List.of(new Subject(Subject.Kind.USER,ACTOR.subject()),new Subject(Subject.Kind.GROUP,"team"))){
   var rows=List.of(new DataRule(subject,READ,Effect.ALLOW,new Condition.All(),DataRule.Target.ROW,Set.of()));
   assertTrue(new DataAccessPlan(both(),rows).canAccess(EAST));
   assertFalse(new DataAccessPlan(decision(List.of()),rows).canAccess(EAST));
  }
 }
 @Test void unrelatedSubjectResourceAndScopeDoNotGrant(){
  var other=new PermissionRequest("sales.order","export");
  assertFalse(new RolePermissionEvaluator(Set.of(READ),List.of()).evaluate(ACTOR,other).allowed());
  assertThrows(IllegalArgumentException.class,()->new RolePermissionEvaluator(Set.of(READ),List.of(new RoleGrant("east",other,Effect.ALLOW))));
  var r=new DataRule(new Subject(Subject.Kind.USER,"another:user"),READ,Effect.ALLOW,new Condition.All(),DataRule.Target.ROW,Set.of());
  assertFalse(new DataAccessPlan(both(),List.of(r)).canAccess(EAST));
 }
 @Test void fieldDenyWinsAndDoesNotDenyWholeRow(){
  var p=new DataAccessPlan(both(),List.of(
   rule("east",Effect.ALLOW,new Condition.All(),DataRule.Target.ROW),
   rule("east",Effect.ALLOW,new Condition.All(),DataRule.Target.FIELD_WRITE,"cost"),
   rule("south",Effect.DENY,new Condition.Equal("region","east"),DataRule.Target.FIELD_WRITE,"cost")));
  assertTrue(p.canAccess(EAST)); assertFalse(p.canWrite(EAST,"cost")); assertTrue(p.canWrite(SOUTH,"cost"));
 }
 @Test void absenceDoesNotMatchExplicitNull(){
  var condition=new Condition.Equal("owner",null);
  assertFalse(condition.matches(Map.of()));
  Map<String,Object> row=new HashMap<>();row.put("owner",null);assertTrue(condition.matches(row));
 }
 @Test void malformedInputsFailRatherThanAllow(){
  assertThrows(IllegalArgumentException.class,()->new PermissionRequest("", "read"));
  assertThrows(IllegalArgumentException.class,()->new FunctionDecision(ACTOR,READ,true,Set.of("admin")));
  assertThrows(IllegalArgumentException.class,()->new Condition.Equal("owner",new Object()));
  assertThrows(NullPointerException.class,()->new DataAccessPlan(both(),List.of()).restrict(null));
 }
}
