package cn.sparrowmini.admin;

import cn.sparrowmini.security.api.*;
import cn.sparrowmini.rbac.persistence.jpa.JpaRoleProvider;
import cn.sparrowmini.identity.local.*;
import cn.sparrowmini.group.persistence.jpa.JpaGroupService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Set;

/** Namespace-wide administration is an explicit capability, never a username special case. */
public final class AdminAccess implements CurrentActorProvider {
    private final String namespace;
    private final ObjectProvider<JpaRoleProvider> roles;
    private final ObjectProvider<JpaGroupService> groups;
    private final ObjectProvider<LocalIdentityProvider> users;
    private final ThreadLocal<Boolean> initializing=ThreadLocal.withInitial(()->false);
    public AdminAccess(String namespace,ObjectProvider<JpaRoleProvider> roles,ObjectProvider<JpaGroupService> groups,ObjectProvider<LocalIdentityProvider> users){
        this.namespace=namespace;this.roles=roles;this.groups=groups;this.users=users;
    }
    public ActorContext current(){
        var authentication=SecurityContextHolder.getContext().getAuthentication();
        if(authentication==null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof LocalPrincipal principal)
            || !namespace.equals(principal.namespace()) || !users.getObject().isCurrent(principal))throw new SecurityException("Current local identity required");
        String subject=namespace+":"+principal.subjectId();
        return new ActorContext(subject,Set.of(),groups.getObject().facts().graph().effectiveGroups(subject));
    }
    public void require(String resource,String operation){
        if(initializing.get())return;
        String scope=operation.equals("read")?"read":"manage";
        if(!roles.getObject().evaluate(current(),new PermissionRequest(resource,scope)).allowed())throw new SecurityException("Management permission denied");
    }
    // Package-private; only the offline bootstrap runner uses this bounded transaction scope.
    void initialize(Runnable work){if(initializing.get())throw new IllegalStateException("Nested bootstrap");initializing.set(true);try{work.run();}finally{initializing.remove();}}
}
