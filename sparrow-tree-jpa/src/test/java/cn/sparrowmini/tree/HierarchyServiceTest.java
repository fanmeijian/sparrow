package cn.sparrowmini.tree;

import jakarta.persistence.*;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import static org.junit.jupiter.api.Assertions.*;
class HierarchyServiceTest {
    static EntityManagerFactory factory;EntityManager em;HierarchyService service;String namespace;
    @BeforeAll static void open(){factory=Persistence.createEntityManagerFactory("tree-test");}
    @AfterAll static void close(){factory.close();}
    @BeforeEach void setup(){em=factory.createEntityManager();namespace=UUID.randomUUID().toString();service=new HierarchyService(em,namespace,(op,id,before,after)->{},node->node.resource()==null);}
    @AfterEach void cleanup(){em.close();}
    <T>T tx(Supplier<T> action){em.getTransaction().begin();try{T value=action.get();em.getTransaction().commit();em.clear();return value;}catch(RuntimeException e){em.getTransaction().rollback();em.clear();throw e;}}
    @Test void persistedMoveRequiresRevisionAndBindingDeletionCheck(){
        var hierarchy=tx(()->service.create("Tree",HierarchyGraph.Mode.TREE));var first=tx(()->service.add(hierarchy.id(),0,null,null,BigDecimal.ZERO));
        String a=first.graph().nodes().keySet().iterator().next();var second=tx(()->service.add(hierarchy.id(),1,"invoice","one",BigDecimal.ONE));
        String b=second.graph().nodes().keySet().stream().filter(id->!id.equals(a)).findFirst().orElseThrow();
        var linked=tx(()->service.link(hierarchy.id(),2,a,b,BigDecimal.ZERO));assertEquals(3,linked.revision());
        assertThrows(OptimisticLockException.class,()->tx(()->service.detach(hierarchy.id(),2,a,b)));
        assertThrows(IllegalStateException.class,()->tx(()->service.deleteSubtree(hierarchy.id(),3,a)));
        assertEquals(2,tx(()->service.get(hierarchy.id())).graph().nodes().size());
    }
    @Test void concurrentOppositeEdgesCannotCreateCycle()throws Exception{
        var hierarchy=tx(()->service.create("Concurrent",HierarchyGraph.Mode.DAG));tx(()->service.add(hierarchy.id(),0,null,null,BigDecimal.ZERO));var two=tx(()->service.add(hierarchy.id(),1,null,null,BigDecimal.ZERO));
        var ids=new ArrayList<>(two.graph().nodes().keySet());var start=new CountDownLatch(1);var executor=Executors.newFixedThreadPool(2);
        try{
            List<Future<Boolean>> results=new ArrayList<>();for(int i=0;i<2;i++){final int direction=i;results.add(executor.submit(()->{try(var other=factory.createEntityManager()){
                var repository=new HierarchyService(other,namespace,(op,id,before,after)->{},node->true);start.await();other.getTransaction().begin();
                try{repository.link(hierarchy.id(),2,ids.get(direction),ids.get(1-direction),BigDecimal.ZERO);other.getTransaction().commit();return true;}
                catch(OptimisticLockException e){other.getTransaction().rollback();return false;}
            }}));}start.countDown();int changed=0;for(var result:results)if(result.get(10,TimeUnit.SECONDS))changed++;assertEquals(1,changed);
        }finally{executor.shutdownNow();}
        assertEquals(1,tx(()->service.get(hierarchy.id())).graph().edges().size());
    }
    @Test void namespaceAndCrossHierarchyEdgesStayIsolated(){
        var one=tx(()->service.create("One",HierarchyGraph.Mode.TREE));var node=tx(()->service.add(one.id(),0,null,null,BigDecimal.ZERO));
        var two=tx(()->service.create("Two",HierarchyGraph.Mode.TREE));var other=tx(()->service.add(two.id(),0,null,null,BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class,()->tx(()->service.link(one.id(),1,node.graph().nodes().keySet().iterator().next(),other.graph().nodes().keySet().iterator().next(),BigDecimal.ZERO)));
        var alien=new HierarchyService(em,"other",(op,id,before,after)->{},n->true);assertThrows(IllegalArgumentException.class,()->tx(()->alien.get(one.id())));
    }
}
