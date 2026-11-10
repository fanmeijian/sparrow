package cn.sparrowmini.rbac.autoconfigure;

import cn.sparrowmini.rbac.core.*;
import cn.sparrowmini.rbac.persistence.jpa.JpaRoleProvider;
import cn.sparrowmini.rbac.persistence.jpa.grant.JpaRoleGrantStore;
import cn.sparrowmini.security.api.*;
import jakarta.persistence.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.*;
import org.springframework.orm.jpa.*;
import org.springframework.transaction.PlatformTransactionManager;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
class RbacStarterTest {
    static final PermissionRequest READ=new PermissionRequest("invoice","read"),EXPORT=new PermissionRequest("invoice","export");
    final ApplicationContextRunner runner=new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(LocalRbacAutoConfiguration.class,ExternalRoleGrantAutoConfiguration.class,RbacSelectionAutoConfiguration.class));
    @Configuration(proxyBeanMethods=false) static class LocalDatabase {
        @Bean(destroyMethod="close") EntityManagerFactory entityManagerFactory(){return Persistence.createEntityManagerFactory("starter-rbac-test");}
        @Bean EntityManager entityManager(EntityManagerFactory factory){return SharedEntityManagerCreator.createSharedEntityManager(factory);}
        @Bean PlatformTransactionManager transactionManager(EntityManagerFactory factory){return new JpaTransactionManager(factory);}
        @Bean PermissionCatalog permissions(){return catalog(Set.of(READ,EXPORT));}
        @Bean RbacManagementGuard guard(){return (n,op,id)->{};}
    }
    static class ExternalRoles implements RoleDirectoryProvider,RoleAssignmentProvider {
        boolean assigned=true;
        public String code(){return "external";}
        public List<RoleProfile> list(int offset,int limit){return List.of(new RoleProfile("realm:reader","reader","Reader",true,0));}
        public RoleProfile create(String code,String name){throw new UnsupportedOperationException();}
        public RoleProfile setEnabled(String id,long version,boolean enabled){throw new UnsupportedOperationException();}
        public Set<Subject> members(String id){return Set.of(new Subject(Subject.Kind.USER,"realm:alice"));}
        public Set<String> rolesFor(Subject subject){return assigned&&subject.id().equals("realm:alice")?Set.of("realm:reader"):Set.of();}
        public void assign(String role,Subject subject){assigned=true;}
        public void revoke(String role,Subject subject){assigned=false;}
    }
    @Configuration(proxyBeanMethods=false) static class ExternalDatabase {
        @Bean(destroyMethod="close") EntityManagerFactory entityManagerFactory(){return Persistence.createEntityManagerFactory("starter-external-role-test");}
        @Bean EntityManager entityManager(EntityManagerFactory factory){return SharedEntityManagerCreator.createSharedEntityManager(factory);}
        @Bean PlatformTransactionManager transactionManager(EntityManagerFactory factory){return new JpaTransactionManager(factory);}
        @Bean PermissionCatalog permissions(){return catalog(Set.of(READ,EXPORT));}
        @Bean ExternalRoles roles(){return new ExternalRoles();}
        @Bean RoleReferenceValidator references(){return id->{if(!id.equals("realm:reader"))throw new IllegalArgumentException("Unknown external role");};}
        @Bean GrantManagementGuard guard(){return (n,op,role,permission,before,after)->{if(EXPORT.equals(permission)||before==Effect.DENY&&after==null)throw new SecurityException("Outside delegated boundary");};}
    }
    @Test void localRbacNeedsNoDataPolicyModule(){runner.withUserConfiguration(LocalDatabase.class).run(c->{
        assertThat(c).hasNotFailed().hasSingleBean(JpaRoleProvider.class);var roles=c.getBean(JpaRoleProvider.class);var role=roles.create("reader","Reader");roles.assign(role.id(),new Subject(Subject.Kind.USER,"local:alice"));roles.grant(role.id(),READ,Effect.ALLOW);
        assertTrue(roles.evaluate(new ActorContext("local:alice",Set.of(),Set.of()),READ).allowed());
    });}
    @Test void externalRolesUseGrantTableWithoutLocalRoleCatalog(){runner.withPropertyValues("sparrow.rbac.provider=external").withUserConfiguration(ExternalDatabase.class).run(c->{
        assertThat(c).hasNotFailed().doesNotHaveBean(JpaRoleProvider.class);var store=c.getBean(JpaRoleGrantStore.class);var roles=c.getBean(ExternalRoles.class);var evaluator=c.getBean(FunctionPermissionEvaluator.class);
        assertEquals(1,c.getBean(EntityManagerFactory.class).getMetamodel().getEntities().size());
        store.grant("realm:reader",READ,Effect.ALLOW);var actor=new ActorContext("realm:alice",Set.of("forged"),Set.of());assertTrue(evaluator.evaluate(actor,READ).allowed());
        roles.assigned=false;assertFalse(evaluator.evaluate(actor,READ).allowed());roles.assigned=true;
        assertThrows(SecurityException.class,()->store.grant("realm:reader",EXPORT,Effect.ALLOW));
        store.grant("realm:reader",READ,Effect.DENY);assertThrows(SecurityException.class,()->store.revokePermission("realm:reader",READ));assertFalse(evaluator.evaluate(actor,READ).allowed());
    });}
    @Test void missingExternalProviderFailsWithoutLocalFallback(){runner.withPropertyValues("sparrow.rbac.provider=external").run(c->assertThat(c).hasFailed());}
 static PermissionCatalog catalog(Set<PermissionRequest> values){return new PermissionCatalog(){
 public Set<PermissionRequest> permissions(){return values;}
 public boolean exists(PermissionRequest p){return values.contains(p);}
 public boolean isEnabled(PermissionRequest p){return values.contains(p);}
 };}
}
