package cn.sparrowmini.system.configuration;

import jakarta.persistence.*;
import org.junit.jupiter.api.*;
import java.util.*;
import java.util.function.Supplier;
import static org.junit.jupiter.api.Assertions.*;
class ConfigurationServiceTest {
    static EntityManagerFactory factory;EntityManager em;ConfigurationService service;
    @BeforeAll static void open(){factory=Persistence.createEntityManagerFactory("system-test");}
    @AfterAll static void close(){factory.close();}
    @BeforeEach void setup(){em=factory.createEntityManager();service=new ConfigurationService(em,UUID.randomUUID().toString(),(op,code)->{},List.of(
        new ConfigurationService.Definition("number",ConfigurationService.Kind.DECIMAL,"1.0",true),new ConfigurationService.Definition("flag",ConfigurationService.Kind.BOOLEAN,"false",true),new ConfigurationService.Definition("private",ConfigurationService.Kind.STRING,"private-value",false)));}
    @AfterEach void cleanup(){em.close();}
    <T>T tx(Supplier<T> action){em.getTransaction().begin();try{T value=action.get();em.getTransaction().commit();em.clear();return value;}catch(RuntimeException e){em.getTransaction().rollback();em.clear();throw e;}}
    @Test void defaultsPrecisionAndVersionedReset(){
        assertEquals("1",service.get("number").value());assertTrue(service.get("number").defaulted());
        var saved=tx(()->service.save("number",null,"12345678901234567890.123456789"));assertEquals("12345678901234567890.123456789",saved.value());
        assertThrows(OptimisticLockException.class,()->tx(()->service.save("number",null,"2")));
        assertTrue(tx(()->service.reset("number",saved.version())).defaulted());
    }
    @Test void unknownTypesAndInvalidBooleansFailWithoutMutation(){
        assertThrows(IllegalArgumentException.class,()->tx(()->service.save("unknown",null,"x")));
        assertThrows(IllegalArgumentException.class,()->tx(()->service.save("flag",null,"yes")));
        assertThrows(IllegalArgumentException.class,()->tx(()->service.save("number",null,"1e1000000")));
        assertEquals("false",service.get("flag").value());
    }
    @Test void onlyExplicitPublicValuesReachClients(){
        assertEquals(Set.of("number","flag"),service.clientValues().keySet());
        var denied=new ConfigurationService(em,"n",(op,code)->{throw new SecurityException();},List.of());assertThrows(SecurityException.class,denied::list);
    }
}
