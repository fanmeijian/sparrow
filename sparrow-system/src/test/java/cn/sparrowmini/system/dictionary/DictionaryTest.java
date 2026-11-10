package cn.sparrowmini.system.dictionary;
import jakarta.persistence.*;
import org.junit.jupiter.api.*;
import java.util.*;
import java.util.function.Supplier;
import static org.junit.jupiter.api.Assertions.*;
class DictionaryTest {
    static EntityManagerFactory emf;EntityManager em;DictionaryService service;
    @BeforeAll static void open(){emf=Persistence.createEntityManagerFactory("system-test");}
    @AfterAll static void close(){emf.close();}
    @BeforeEach void setup(){em=emf.createEntityManager();service=new DictionaryService(em,UUID.randomUUID().toString(),(n,s,d)->{},(d,e)->false);}
    @AfterEach void clean(){em.close();}
    <T>T tx(Supplier<T> action){em.getTransaction().begin();try{T r=action.get();em.getTransaction().commit();em.clear();return r;}catch(RuntimeException e){em.getTransaction().rollback();em.clear();throw e;}}
    @Test void disablingEntryPreservesHistoricalLabelAndCode(){
        tx(()->service.create("source","Customer source"));var entry=tx(()->service.add("source","expo","Exhibition",1));
        var disabled=tx(()->service.update("source",entry.id(),entry.version(),"Trade show",2,false));
        assertEquals("expo",disabled.code());assertTrue(service.entries("source",true,0,10).isEmpty());
        assertEquals("Trade show",service.entries("source",false,0,10).get(0).label());
        assertThrows(IllegalStateException.class,()->tx(()->{service.deleteEntry("source",entry.id(),disabled.version());return null;}));
    }
    @Test void staleAndUnauthorizedUpdatesCannotChangeValues(){
        tx(()->service.create("source","Source"));var entry=tx(()->service.add("source","expo","Exhibition",1));
        tx(()->service.update("source",entry.id(),entry.version(),"New",2,true));
        assertThrows(OptimisticLockException.class,()->tx(()->service.update("source",entry.id(),entry.version(),"Stale",3,true)));
        var denied=new DictionaryService(em,"tenant",(n,s,d)->{throw new SecurityException();},(d,e)->true);
        assertThrows(SecurityException.class,()->tx(()->denied.create("x","X")));
    }
    @Test void dictionariesAreNamespaceScopedAndQueriesBounded(){
        tx(()->service.create("source","Source"));var other=new DictionaryService(em,"other",(n,s,d)->{},(d,e)->false);
        assertTrue(other.definitions(0,10).isEmpty());assertThrows(IllegalArgumentException.class,()->service.definitions(0,501));
    }
}
