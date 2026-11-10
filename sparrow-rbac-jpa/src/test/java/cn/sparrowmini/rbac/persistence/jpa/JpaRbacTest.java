package cn.sparrowmini.rbac.persistence.jpa;

import cn.sparrowmini.rbac.core.*;
import cn.sparrowmini.rbac.persistence.jpa.grant.RolePermissionEntity;
import cn.sparrowmini.security.api.*;
import jakarta.persistence.*;
import org.junit.jupiter.api.*;
import java.util.*;
import java.util.function.Supplier;
import static org.junit.jupiter.api.Assertions.*;

class JpaRbacTest {
    static EntityManagerFactory emf; EntityManager em; JpaRoleProvider roles;
    static final PermissionRequest READ=new PermissionRequest("order","read"),EXPORT=new PermissionRequest("order","export");
    static final Subject ALICE=new Subject(Subject.Kind.USER,"tenant:alice");
    static final ActorContext ACTOR=new ActorContext(ALICE.id(),Set.of("forged-role"),Set.of("tenant:team"));
    @BeforeAll static void open(){emf=Persistence.createEntityManagerFactory("rbac-test");}
    @AfterAll static void close(){emf.close();}
    @BeforeEach void setup(){
        em=emf.createEntityManager();roles=new JpaRoleProvider(em,"tenant",(n,o,r)->{},catalog(Set.of(READ,EXPORT)));
        tx(()->{for(var type:List.of(RolePermissionEntity.class,RoleAssignmentEntity.class,RoleEntity.class)){
            deleteAll(type);
        }return null;});
    }
    <T> void deleteAll(Class<T> type){var cb=em.getCriteriaBuilder();var q=cb.createCriteriaDelete(type);q.from(type);em.createQuery(q).executeUpdate();}
    @AfterEach void cleanup(){if(em.getTransaction().isActive())em.getTransaction().rollback();em.close();}
    <T>T tx(Supplier<T> action){em.getTransaction().begin();try{T result=action.get();em.getTransaction().commit();em.clear();return result;}catch(RuntimeException e){em.getTransaction().rollback();em.clear();throw e;}}
    RoleProfile role(String code,Subject subject,PermissionRequest permission,Effect effect){return tx(()->{var r=roles.create(code,code);roles.assign(r.id(),subject);roles.grant(r.id(),permission,effect);return r;});}
    @Test void currentAssignmentOverridesStaleClaimsAndRevocationTakesEffect(){
        assertFalse(roles.evaluate(ACTOR,READ).allowed());
        var r=role("reader",ALICE,READ,Effect.ALLOW);assertTrue(roles.evaluate(ACTOR,READ).allowed());
        assertEquals(Set.of(r.id()),roles.evaluate(ACTOR,READ).eligibleRoles());
        tx(()->{roles.revoke(r.id(),ALICE);return null;});assertFalse(roles.evaluate(ACTOR,READ).allowed());
    }
    @Test void groupRolesAndExplicitDenyCompose(){
        role("reader",new Subject(Subject.Kind.GROUP,"tenant:team"),READ,Effect.ALLOW);
        assertTrue(roles.evaluate(ACTOR,READ).allowed());
        role("blocked",ALICE,READ,Effect.DENY);assertFalse(roles.evaluate(ACTOR,READ).allowed());
        assertFalse(roles.evaluate(new ActorContext("tenant:bob",Set.of(),Set.of()),READ).allowed());
    }
    @Test void scopeEvidenceDoesNotCrossRoles(){
        var read=role("reader",ALICE,READ,Effect.ALLOW);var export=role("exporter",ALICE,EXPORT,Effect.ALLOW);
        assertEquals(Set.of(read.id()),roles.evaluate(ACTOR,READ).eligibleRoles());
        assertEquals(Set.of(export.id()),roles.evaluate(ACTOR,EXPORT).eligibleRoles());
    }
    @Test void disablingRoleRemovesEffectiveGrant(){
        var r=role("reader",ALICE,READ,Effect.ALLOW);
        tx(()->roles.setEnabled(r.id(),r.version(),false));assertFalse(roles.evaluate(ACTOR,READ).allowed());
        assertThrows(OptimisticLockException.class,()->tx(()->roles.setEnabled(r.id(),r.version(),true)));
    }
    @Test void assignmentIsIdempotentAndNamespaceIsolated(){
        var r=role("reader",ALICE,READ,Effect.ALLOW);
        tx(()->{roles.assign(r.id(),ALICE);return null;});assertEquals(Set.of(r.id()),roles.rolesFor(ALICE));
        var other=new JpaRoleProvider(em,"other",(n,o,id)->{},catalog(Set.of(READ)));assertFalse(other.evaluate(ACTOR,READ).allowed());
        assertThrows(IllegalArgumentException.class,()->tx(()->{other.assign(r.id(),ALICE);return null;}));
    }
    @Test void guardsAndRegistrationPreventUnauthorizedChanges(){
        var r=role("reader",ALICE,READ,Effect.ALLOW);
        var denied=new JpaRoleProvider(em,"tenant",(n,o,id)->{throw new SecurityException();},catalog(Set.of(READ)));
        assertThrows(SecurityException.class,()->tx(()->{denied.assign(r.id(),new Subject(Subject.Kind.USER,"tenant:bob"));return null;}));
        assertThrows(IllegalArgumentException.class,()->tx(()->{roles.grant(r.id(),new PermissionRequest("unknown","read"),Effect.ALLOW);return null;}));
        assertTrue(roles.rolesFor(new Subject(Subject.Kind.USER,"tenant:bob")).isEmpty());
    }
 static PermissionCatalog catalog(Set<PermissionRequest> values){return new PermissionCatalog(){
 public Set<PermissionRequest> permissions(){return values;}
 public boolean exists(PermissionRequest p){return values.contains(p);}
 public boolean isEnabled(PermissionRequest p){return values.contains(p);}
 };}
}
