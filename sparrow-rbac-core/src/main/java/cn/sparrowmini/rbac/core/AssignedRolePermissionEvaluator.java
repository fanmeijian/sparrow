package cn.sparrowmini.rbac.core;

import cn.sparrowmini.security.api.*;
import java.util.*;
/** Identity-provider independent RBAC evaluation using freshly resolved role assignments. */
public final class AssignedRolePermissionEvaluator implements FunctionPermissionEvaluator {
    @Override public boolean supportsRoleEvidence(){return true;}
    private final RoleAssignmentProvider assignments;private final RoleGrantSource grants;private final Set<PermissionRequest> registered;
    public AssignedRolePermissionEvaluator(RoleAssignmentProvider assignments,RoleGrantSource grants,Set<PermissionRequest> registered){this.assignments=Objects.requireNonNull(assignments);this.grants=Objects.requireNonNull(grants);this.registered=Set.copyOf(registered);}
    public FunctionDecision evaluate(ActorContext actor,PermissionRequest request){
        Set<String> current=new HashSet<>(assignments.rolesFor(new Subject(Subject.Kind.USER,actor.subject())));
        for(String group:actor.groups())current.addAll(assignments.rolesFor(new Subject(Subject.Kind.GROUP,group)));
        var trusted=new ActorContext(actor.subject(),current,actor.groups());
        if(!registered.contains(request)||current.isEmpty())return new FunctionDecision(trusted,request,false,Set.of());
        return new RolePermissionEvaluator(registered,grants.find(Set.copyOf(current),request)).evaluate(trusted,request);
    }
}
