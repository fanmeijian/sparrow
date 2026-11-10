package cn.sparrowmini.rbac.core;
import cn.sparrowmini.security.api.*;
import java.util.*;
/** Immutable grant snapshot. Caller resolves role/group inheritance before evaluation. */
public final class RolePermissionEvaluator implements FunctionPermissionEvaluator {
    @Override public boolean supportsRoleEvidence(){return true;}
 private final Set<PermissionRequest> registered;
 private final List<RoleGrant> grants;
 public RolePermissionEvaluator(Set<PermissionRequest> registered, List<RoleGrant> grants) {
  this.registered=Set.copyOf(registered); this.grants=List.copyOf(grants);
  if(grants.stream().anyMatch(g -> !registered.contains(g.permission()))) throw new IllegalArgumentException("Unregistered permission");
 }
 public FunctionDecision evaluate(ActorContext actor, PermissionRequest request) {
  Objects.requireNonNull(actor); Objects.requireNonNull(request);
  if(!registered.contains(request)) return new FunctionDecision(actor,request,false,Set.of());
  Set<String> eligible=new HashSet<>();
  for(RoleGrant grant:grants) if(grant.permission().equals(request) && actor.roles().contains(grant.role())) {
   if(grant.effect()==Effect.DENY) return new FunctionDecision(actor,request,false,Set.of());
   eligible.add(grant.role());
  }
  return new FunctionDecision(actor,request,!eligible.isEmpty(),eligible);
 }
}
