package cn.sparrowmini.group.persistence.jpa;
import cn.sparrowmini.group.core.*;
import jakarta.persistence.*;
import org.junit.jupiter.api.*;
import java.util.*;
import java.util.function.Supplier;
import static org.junit.jupiter.api.Assertions.*;
class JpaGroupTest {
    static EntityManagerFactory emf;EntityManager em;JpaGroupService service;
    @BeforeAll static void open(){emf=Persistence.createEntityManagerFactory("group-test");}
    @AfterAll static void close(){emf.close();}
    @BeforeEach void setup(){
        em=emf.createEntityManager();service=new JpaGroupService(em,"tenant",(o,b,a)->{});
        tx(()->{for(var c:List.of(GroupMemberEntity.class,GroupEdgeEntity.class,GroupNodeEntity.class,GroupNamespace.class))delete(c);service.initialize();return null;});
    }
    <T>void delete(Class<T> c){var cb=em.getCriteriaBuilder();var q=cb.createCriteriaDelete(c);q.from(c);em.createQuery(q).executeUpdate();}
    <T>T tx(Supplier<T> action){em.getTransaction().begin();try{T r=action.get();em.getTransaction().commit();em.clear();return r;}catch(RuntimeException e){em.getTransaction().rollback();em.clear();throw e;}}
    @AfterEach void cleanup(){em.close();}
    @Test void persistsMembershipAndRejectsStaleMutations(){
        var a=tx(()->service.create(0,"parent","Parent"));var b=tx(()->service.create(a.revision(),"child","Child"));
        var c=tx(()->service.addChild(b.revision(),"tenant:parent","tenant:child"));
        tx(()->service.addMember(c.revision(),"tenant:child","alice"));
        assertEquals(Set.of("tenant:parent","tenant:child"),tx(service::facts).graph().effectiveGroups("alice"));
        assertThrows(OptimisticLockException.class,()->tx(()->service.removeChild(c.revision(),"tenant:parent","tenant:child")));
    }
    @Test void cycleFailureRollsBackAndNamespaceIsIsolated(){
        tx(()->service.create(0,"parent","Parent"));tx(()->service.create(1,"child","Child"));tx(()->service.addChild(2,"tenant:parent","tenant:child"));
        assertThrows(IllegalArgumentException.class,()->tx(()->service.addChild(3,"tenant:child","tenant:parent")));
        assertEquals(3,tx(service::facts).revision());
        var other=new JpaGroupService(em,"other",(o,b,a)->{});tx(()->{other.initialize();return null;});assertTrue(tx(other::facts).graph().nodes().isEmpty());
    }
    @Test void delegationGuardSeesBeforeAndAfterAndCanRejectRemoval(){
        tx(()->service.create(0,"restricted","Restricted"));tx(()->service.addMember(1,"tenant:restricted","alice"));
        var controlled=new JpaGroupService(em,"tenant",(operation,before,after)->{
            if(before.effectiveGroups("alice").contains("tenant:restricted") && !after.effectiveGroups("alice").contains("tenant:restricted"))throw new SecurityException("Cannot remove deny membership");
        });
        assertThrows(SecurityException.class,()->tx(()->controlled.removeMember(2,"tenant:restricted","alice")));
        assertEquals(Set.of("tenant:restricted"),tx(service::facts).graph().effectiveGroups("alice"));assertEquals(2,tx(service::facts).revision());
    }
}
