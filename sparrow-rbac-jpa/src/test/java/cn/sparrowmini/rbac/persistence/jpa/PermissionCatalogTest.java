package cn.sparrowmini.rbac.persistence.jpa;
import cn.sparrowmini.rbac.persistence.jpa.catalog.*;
import cn.sparrowmini.rbac.persistence.jpa.grant.JpaRoleGrantStore;
import cn.sparrowmini.rbac.core.RolePermissionEvaluator;
import cn.sparrowmini.security.api.*;
import jakarta.persistence.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class PermissionCatalogTest {
 @Test void changesAreLiveIsolatedAndDisabledGrantsRemainRevocable(){
  try(var factory=Persistence.createEntityManagerFactory("external-role-test");var em=factory.createEntityManager()){
   var catalog=new JpaPermissionCatalog(em,"tenant",op->{});var other=new JpaPermissionCatalog(em,"other",op->{});
   var store=new JpaRoleGrantStore(em,"tenant",catalog,id->{},(n,o,r,p,b,a)->{});var read=new PermissionRequest("order","read");
   em.getTransaction().begin();catalog.saveResource(new JpaPermissionCatalog.Resource("order","Orders","",true,-1));catalog.saveScope(new JpaPermissionCatalog.Scope("order","read","Read","",true,-1));store.grant("reader",read,Effect.ALLOW);em.getTransaction().commit();em.clear();
   assertTrue(catalog.isEnabled(read));assertFalse(other.exists(read));
   var evaluator=new RolePermissionEvaluator(catalog,store.find(Set.of("reader"),read));var actor=new ActorContext("alice",Set.of("reader"),Set.of());assertTrue(evaluator.evaluate(actor,read).allowed());
   var resource=catalog.resources().get(0);em.getTransaction().begin();catalog.saveResource(new JpaPermissionCatalog.Resource("order","Orders","",false,resource.version()));em.getTransaction().commit();em.clear();
   assertTrue(catalog.exists(read));assertFalse(catalog.isEnabled(read));assertFalse(evaluator.evaluate(actor,read).allowed());assertTrue(catalog.permissions().isEmpty());
   em.getTransaction().begin();assertThrows(IllegalArgumentException.class,()->store.grant("new",read,Effect.ALLOW));store.revokePermission("reader",read);em.getTransaction().commit();assertTrue(store.grants("reader").isEmpty());
   em.getTransaction().begin();assertThrows(OptimisticLockException.class,()->catalog.saveResource(resource));em.getTransaction().rollback();
  }
 }
}
