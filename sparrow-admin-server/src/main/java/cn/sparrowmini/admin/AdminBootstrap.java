package cn.sparrowmini.admin;

import cn.sparrowmini.identity.local.*;
import cn.sparrowmini.rbac.persistence.jpa.*;
import cn.sparrowmini.group.persistence.jpa.*;
import cn.sparrowmini.security.api.*;
import jakarta.persistence.EntityManager;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import java.util.Set;

@Configuration(proxyBeanMethods=false)
@ConditionalOnProperty(name="sparrow.bootstrap.enabled",havingValue="true")
public class AdminBootstrap {
    @Bean ApplicationRunner initializeAdmin(Environment env,AdminAccess access,EntityManager em,LocalIdentityProvider users,JpaRoleProvider roles,JpaGroupService groups,PlatformTransactionManager manager,PermissionCatalog registered,cn.sparrowmini.system.audit.AuditService audit){
        if(!"none".equals(env.getProperty("spring.main.web-application-type")))throw new IllegalStateException("Bootstrap requires offline non-web process");
        String username=env.getRequiredProperty("sparrow.bootstrap.username"),password=env.getRequiredProperty("sparrow.bootstrap.password");
        String namespace=env.getProperty("sparrow.identity.namespace","default");
        return args->access.initialize(()->new TransactionTemplate(manager).executeWithoutResult(status->{
            groups.initialize();
            em.find(GroupNamespace.class,namespace,jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
            var cb=em.getCriteriaBuilder();var q=cb.createQuery(Long.class);var root=q.from(LocalAccount.class);
            q.select(cb.count(root)).where(cb.equal(root.get("namespace"),namespace));
            if(em.createQuery(q).getSingleResult()!=0)throw new IllegalStateException("Bootstrap requires empty identity namespace");
            var admin=users.create(username,"Administrator",password);var role=roles.create("administrator","Administrator");
            roles.assign(role.id(),new Subject(Subject.Kind.USER,namespace+":"+admin.id()));
            // No implicit grants for business data or future capabilities.
            registered.permissions().stream().filter(p->p.resource().startsWith("admin.")).forEach(p->roles.grant(role.id(),p,Effect.ALLOW));
            audit.record(namespace+":bootstrap","BOOTSTRAP","admin.users","SUCCEEDED",java.util.UUID.randomUUID().toString(),null,"OFFLINE_INITIALIZATION");
        }));
    }
}
