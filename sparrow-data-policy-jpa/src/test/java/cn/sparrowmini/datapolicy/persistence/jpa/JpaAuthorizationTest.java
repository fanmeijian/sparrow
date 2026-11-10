package cn.sparrowmini.datapolicy.persistence.jpa;

import cn.sparrowmini.security.api.*;
import cn.sparrowmini.datapolicy.core.*;
import jakarta.persistence.*;
import org.junit.jupiter.api.*;
import org.springframework.data.domain.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class JpaAuthorizationTest {
    static EntityManagerFactory emf;
    EntityManager em;
    static final PermissionRequest READ=new PermissionRequest("sales.order","read");
    static final ActorContext ACTOR=new ActorContext("tenant:alice",Set.of("sales"),Set.of());
    static final JpaResource<OrderRecord> RESOURCE=new JpaResource<>("sales.order",OrderRecord.class,"id",Set.of("id","region","cost","note"));
    @BeforeAll static void open(){emf=Persistence.createEntityManagerFactory("policy-test");}
    @AfterAll static void close(){emf.close();}
    @BeforeEach void seed(){
        em=emf.createEntityManager();em.getTransaction().begin();
        var cb=em.getCriteriaBuilder();var delete=cb.createCriteriaDelete(OrderRecord.class);delete.from(OrderRecord.class);em.createQuery(delete).executeUpdate();
        em.persist(new OrderRecord(1,"east",100));em.persist(new OrderRecord(2,"south",200));
        em.persist(new OrderRecord(3,"east",300));em.persist(new OrderRecord(4,null,400));
        em.getTransaction().commit();em.clear();
    }
    @AfterEach void clean(){if(em.getTransaction().isActive())em.getTransaction().rollback();em.close();}
    DataRule rule(Effect effect,Condition condition,DataRule.Target target,String... fields){
        return new DataRule(new Subject(Subject.Kind.ROLE,"sales"),READ,effect,condition,target,Set.of(fields));
    }
    DataAccessPlan plan(Condition row){return new DataAccessPlan(new FunctionDecision(ACTOR,READ,true,Set.of("sales")),List.of(
        rule(Effect.ALLOW,row,DataRule.Target.ROW),
        rule(Effect.ALLOW,new Condition.All(),DataRule.Target.FIELD_READ,"id","region","note"),
        rule(Effect.ALLOW,new Condition.Equal("region","east"),DataRule.Target.FIELD_READ,"cost")));}
    @Test void rowFilteringHappensBeforePaginationAndCount(){
        var result=new SecuredJpaReader<>(em,RESOURCE).read(plan(new Condition.Equal("region","east")),new Condition.All(),PageRequest.of(1,1),Set.of("id","cost"));
        assertEquals(2,result.getTotalElements());assertEquals(Map.of("id",3L,"cost",300),result.getContent().get(0));
    }
    @Test void mixedRowsNeverUnionTheirFieldRights(){
        var result=new SecuredJpaReader<>(em,RESOURCE).read(plan(new Condition.All()),new Condition.All(),PageRequest.of(0,10),Set.of("id","cost","note"));
        assertEquals(4,result.getTotalElements());assertTrue(result.getContent().get(0).containsKey("cost"));
        assertFalse(result.getContent().get(1).containsKey("cost"));
        assertTrue(result.getContent().get(0).containsKey("note"));assertNull(result.getContent().get(0).get("note"));
    }
    @Test void hiddenFieldCannotBeUsedAsFilterOrSort(){
        var reader=new SecuredJpaReader<>(em,RESOURCE);
        assertThrows(SecurityException.class,()->reader.read(plan(new Condition.All()),new Condition.Equal("cost",200),PageRequest.of(0,10),Set.of("id")));
        assertThrows(SecurityException.class,()->reader.read(plan(new Condition.All()),new Condition.All(),PageRequest.of(0,10,Sort.by("cost")),Set.of("id")));
    }
    @Test void sqlNullSemanticsAgreeWithMemory(){
        var conditions=List.of(new Condition.Not(new Condition.Equal("region","east")),new Condition.Equal("region",null),new Condition.In("region",List.of("east","south")),new Condition.Not(new Condition.In("region",List.of("east"))));
        List<Map<String,Object>> rows=new ArrayList<>();
        for(var tuple:List.of(new Object[]{1L,"east"},new Object[]{2L,"south"},new Object[]{3L,"east"},new Object[]{4L,null})){
            Map<String,Object> map=new HashMap<>();map.put("id",tuple[0]);map.put("region",tuple[1]);rows.add(map);
        }
        for(Condition condition:conditions){
            var actual=new SecuredJpaReader<>(em,RESOURCE).read(plan(condition),new Condition.All(),PageRequest.of(0,10),Set.of("id"));
            assertEquals(rows.stream().filter(condition::matches).map(r->r.get("id")).toList(),actual.getContent().stream().map(r->r.get("id")).toList());
        }
    }
    @Test void invalidPropertiesAndLiteralTypesFailBeforeReading(){
        var reader=new SecuredJpaReader<>(em,RESOURCE);
        assertThrows(IllegalArgumentException.class,()->reader.read(plan(new Condition.Equal("unknown",1)),new Condition.All(),PageRequest.of(0,10),Set.of("id")));
        assertThrows(IllegalArgumentException.class,()->reader.read(plan(new Condition.Equal("id","1")),new Condition.All(),PageRequest.of(0,10),Set.of("id")));
        assertThrows(IllegalArgumentException.class,()->reader.read(plan(new Condition.All()),new Condition.All(),Pageable.unpaged(),Set.of("id")));
    }
    @Test void addedFieldConstraintOnlyRemovesVisibility(){
        var p=plan(new Condition.All()).restrictField("cost",DataRule.Target.FIELD_READ,new Condition.Equal("id",3L));
        var result=new SecuredJpaReader<>(em,RESOURCE).read(p,new Condition.All(),PageRequest.of(0,10),Set.of("id","cost"));
        assertFalse(result.getContent().get(0).containsKey("cost"));assertEquals(300,result.getContent().get(2).get("cost"));
    }
    @Test void deniedFunctionProducesNoRows(){
        var p=new DataAccessPlan(new FunctionDecision(ACTOR,READ,false,Set.of()),List.of(rule(Effect.ALLOW,new Condition.All(),DataRule.Target.ROW)));
        var result=new SecuredJpaReader<>(em,RESOURCE).read(p,new Condition.All(),PageRequest.of(0,10),Set.of("id"));
        assertEquals(0,result.getTotalElements());
    }

    DataAccessPlan updatePlan(){
        var update=new PermissionRequest("sales.order","update");
        var subject=new Subject(Subject.Kind.ROLE,"sales");
        return new DataAccessPlan(new FunctionDecision(ACTOR,update,true,Set.of("sales")),List.of(
            new DataRule(subject,update,Effect.ALLOW,new Condition.Equal("region","east"),DataRule.Target.ROW,Set.of()),
            new DataRule(subject,update,Effect.ALLOW,new Condition.All(),DataRule.Target.FIELD_WRITE,Set.of("region","note"))));
    }
    SecuredJpaUpdater<OrderRecord> updater(){return new SecuredJpaUpdater<>(em,RESOURCE,"version",Set.of("region","cost","note"),(before,after)->{});}
    @Test void patchChecksFieldsTargetStateAndVersionBeforeMutation(){
        em.getTransaction().begin();
        assertThrows(SecurityException.class,()->updater().patch(updatePlan(),1L,0L,Map.of("cost",999)));
        assertThrows(SecurityException.class,()->updater().patch(updatePlan(),1L,0L,Map.of("region","south")));
        assertThrows(SecurityException.class,()->updater().patch(updatePlan(),2L,0L,Map.of("note","outside")));
        assertEquals("east",em.find(OrderRecord.class,1L).region); assertEquals(100,em.find(OrderRecord.class,1L).cost);
        updater().patch(updatePlan(),1L,0L,Map.of("note","approved"));
        assertEquals("approved",em.find(OrderRecord.class,1L).note);assertEquals(1L,em.find(OrderRecord.class,1L).version);
        assertThrows(OptimisticLockException.class,()->updater().patch(updatePlan(),1L,0L,Map.of("note","stale")));
        em.getTransaction().rollback(); em.clear();assertNull(em.find(OrderRecord.class,1L).note);
    }
    @Test void businessGuardCanRejectAndReadPermissionCannotWrite(){
        em.getTransaction().begin();
        var updater=new SecuredJpaUpdater<>(em,RESOURCE,"version",Set.of("note"),(before,after)->{throw new SecurityException("Frozen business record");});
        assertThrows(SecurityException.class,()->updater.patch(updatePlan(),1L,0L,Map.of("note","changed")));
        assertThrows(SecurityException.class,()->updater().patch(plan(new Condition.All()),1L,0L,Map.of("note","changed")));
        assertNull(em.find(OrderRecord.class,1L).note);
    }
}
