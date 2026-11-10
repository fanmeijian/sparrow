package cn.sparrowmini.datapolicy.persistence.jpa;

import cn.sparrowmini.security.api.*;
import cn.sparrowmini.datapolicy.core.*;
import jakarta.persistence.*;
import org.junit.jupiter.api.*;
import java.util.*;
import java.util.function.Supplier;
import static org.junit.jupiter.api.Assertions.*;

class PolicyLifecycleTest {
    static EntityManagerFactory emf;EntityManager em;JpaPolicyStore store;
    static final PermissionRequest READ=new PermissionRequest("sales.order","read");
    static final JpaResource<OrderRecord> RESOURCE=new JpaResource<>("sales.order",OrderRecord.class,"id",Set.of("id","region","cost"));
    @BeforeAll static void open(){emf=Persistence.createEntityManagerFactory("policy-test");}
    @AfterAll static void close(){emf.close();}
    @BeforeEach void setup(){em=emf.createEntityManager();store=new JpaPolicyStore(em,"tenant",(n,o,r)->{},Map.of(READ,RESOURCE));tx(()->{var cb=em.getCriteriaBuilder();var q=cb.createCriteriaDelete(PolicyDocument.class);q.from(PolicyDocument.class);em.createQuery(q).executeUpdate();return null;});}
    @AfterEach void cleanup(){em.close();}
    <T>T tx(Supplier<T> action){em.getTransaction().begin();try{T r=action.get();em.getTransaction().commit();em.clear();return r;}catch(RuntimeException e){if(em.getTransaction().isActive())em.getTransaction().rollback();em.clear();throw e;}}
    DataRule rule(Condition condition){return new DataRule(new Subject(Subject.Kind.USER,"tenant:alice"),READ,Effect.ALLOW,condition,DataRule.Target.ROW,Set.of());}
    @Test void draftDoesNotAuthorizeUntilPublishedAndRetirementRemovesAccess(){
        var rules=List.of(rule(new Condition.Equal("id",1L)));
        var draft=tx(()->store.saveDraft(READ,null,rules));assertTrue(store.active(READ).rules().isEmpty());
        var published=tx(()->store.publish(READ,draft.version()));assertEquals(rules,store.active(READ).rules());assertEquals(1,published.activeRevision());
        var edited=tx(()->store.saveDraft(READ,published.version(),List.of(rule(new Condition.Equal("id",2L)))));
        assertEquals(rules,store.active(READ).rules());
        tx(()->{store.retire(READ,edited.version());return null;});assertTrue(store.active(READ).rules().isEmpty());
    }
    @Test void publicationRequiresCurrentVersionAndDelegation(){
        var draft=tx(()->store.saveDraft(READ,null,List.of(rule(new Condition.All()))));
        tx(()->store.saveDraft(READ,draft.version(),List.of(rule(new Condition.None()))));
        assertThrows(OptimisticLockException.class,()->tx(()->store.publish(READ,draft.version())));
        var denied=new JpaPolicyStore(em,"tenant",(n,o,r)->{throw new SecurityException();},Map.of(READ,RESOURCE));
        assertThrows(SecurityException.class,()->tx(()->denied.publish(READ,store.draft(READ).version())));
        assertTrue(store.active(READ).rules().isEmpty());
    }
    @Test void schemaRejectsUnknownFieldsAndWrongLiteralTypes(){
        assertThrows(IllegalArgumentException.class,()->tx(()->store.saveDraft(READ,null,List.of(rule(new Condition.Equal("secret",1))))));
        assertThrows(IllegalArgumentException.class,()->tx(()->store.saveDraft(READ,null,List.of(rule(new Condition.Equal("id","1"))))));
        assertTrue(store.active(READ).rules().isEmpty());
    }
    @Test void typedJsonRoundTripsWithoutClassLoading(){
        var codec=new PolicyJsonCodec();
        var rules=List.of(rule(new Condition.And(List.of(new Condition.Equal("id",1L),new Condition.In("region",List.of("east","south"))))));
        assertEquals(rules,codec.decode(codec.encode(rules)));
        assertThrows(IllegalArgumentException.class,()->codec.decode("{\"@class\":\"java.lang.Runtime\"}"));
    }
    @Test void namespacesDoNotSharePolicyDocuments(){
        var d=tx(()->store.saveDraft(READ,null,List.of(rule(new Condition.All()))));tx(()->store.publish(READ,d.version()));
        var other=new JpaPolicyStore(em,"other",(n,o,r)->{},Map.of(READ,RESOURCE));assertTrue(other.active(READ).rules().isEmpty());
    }
}
