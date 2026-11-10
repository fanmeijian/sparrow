package cn.sparrowmini.system.navigation;

import cn.sparrowmini.security.api.*;
import jakarta.persistence.*;
import org.junit.jupiter.api.*;
import java.util.*;
import java.util.function.Supplier;
import static org.junit.jupiter.api.Assertions.*;
class NavigationServiceTest {
    static EntityManagerFactory factory;EntityManager em;NavigationService service;
    final PermissionRequest read=new PermissionRequest("invoice","read");
    @BeforeAll static void open(){factory=Persistence.createEntityManagerFactory("system-test");}
    @AfterAll static void close(){factory.close();}
    @BeforeEach void setup(){em=factory.createEntityManager();service=new NavigationService(em,UUID.randomUUID().toString(),op->{},(a,p)->new FunctionDecision(a,p,a.roles().contains("reader"),a.roles().contains("reader")?Set.of("reader"):Set.of()),Set.of(read));}
    @AfterEach void cleanup(){em.close();}
    <T>T tx(Supplier<T> action){em.getTransaction().begin();try{T value=action.get();em.getTransaction().commit();em.clear();return value;}catch(RuntimeException e){em.getTransaction().rollback();em.clear();throw e;}}
    NavigationService.Item item(String code,String parent,boolean visible,String url){return new NavigationService.Item(code,parent,code,url,"0.125",true,visible,visible?null:read);}
    @Test void privateParentRestrictsPublicChildren(){
        tx(()->service.save(null,List.of(item("parent",null,false,"/invoice"),item("child","parent",true,"/invoice/new"))));
        assertTrue(service.visible(new ActorContext("user",Set.of(),Set.of())).isEmpty());
        assertEquals(2,service.visible(new ActorContext("user",Set.of("reader"),Set.of())).size());
    }
    @Test void cyclesExternalTargetsAndUnknownScopesAreRejected(){
        assertThrows(IllegalArgumentException.class,()->tx(()->service.save(null,List.of(item("a","b",true,"/"),item("b","a",true,"/")))));
        assertThrows(IllegalArgumentException.class,()->tx(()->service.save(null,List.of(item("a",null,true,"javascript:alert(1)")))));
        assertThrows(IllegalArgumentException.class,()->tx(()->service.save(null,List.of(item("a",null,true,"//example.com")))));
        assertTrue(service.get().items().isEmpty());
    }
    @Test void updatesRequireCurrentVersion(){
        var saved=tx(()->service.save(null,List.of(item("a",null,true,"/"))));
        assertThrows(OptimisticLockException.class,()->tx(()->service.save(null,List.of())));
        assertTrue(tx(()->service.save(saved.version(),List.of())).items().isEmpty());
    }
}
