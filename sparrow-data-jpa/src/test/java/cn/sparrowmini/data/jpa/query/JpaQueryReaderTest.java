package cn.sparrowmini.data.jpa.query;

import cn.sparrowmini.core.query.Condition;
import jakarta.persistence.*;
import org.junit.jupiter.api.*;
import org.springframework.data.domain.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class JpaQueryReaderTest {
    @MappedSuperclass public static class Base { @Id public Long id; public String owner; }
    @Entity(name="QuerySample") @Table(name="query_sample") public static class Sample extends Base {
        public String title; public String privateNote;
        public Sample() {}
        Sample(long id,String owner,String title){this.id=id;this.owner=owner;this.title=title;this.privateNote="secret";}
    }
    static EntityManagerFactory factory; EntityManager em;
    final JpaResource<Sample> resource = new JpaResource<>("sample",Sample.class,"id",Set.of("id","owner","title"));
    @BeforeAll static void start(){factory=Persistence.createEntityManagerFactory("query-test");}
    @AfterAll static void stop(){factory.close();}
    @BeforeEach void setup(){em=factory.createEntityManager();em.getTransaction().begin();em.createQuery("delete from QuerySample").executeUpdate();em.persist(new Sample(1,"a","first"));em.persist(new Sample(2,"b","other"));em.persist(new Sample(3,"a",null));em.flush();}
    @AfterEach void close(){em.getTransaction().rollback();em.close();}
    @Test void ownerConstraintPrecedesPaginationCountAndProjection(){
        var owner=new AtomicReference<>("a");
        var reader=new JpaQueryReader<>(em,resource,()->(root,q,cb)->cb.equal(root.get("owner"),owner.get()));
        var page=reader.read(new Condition.All(),PageRequest.of(1,1),Set.of("title"));
        assertEquals(2,page.getTotalElements());assertEquals(1,page.getContent().size());
        assertTrue(page.getContent().get(0).containsKey("title"));assertNull(page.getContent().get(0).get("title"));
        assertFalse(page.getContent().get(0).containsKey("privateNote"));
        owner.set("b");assertEquals("other",reader.read(new Condition.All(),PageRequest.of(0,10),Set.of("title")).getContent().get(0).get("title"));
    }
    @Test void missingConstraintNeverFallsBackToUnrestrictedQuery(){
        assertThrows(NullPointerException.class,()->new JpaQueryReader<>(em,resource,()->null).read(new Condition.All(),PageRequest.of(0,10),Set.of("id")));
        assertThrows(NullPointerException.class,()->new JpaQueryReader<>(em,resource,()->(r,q,c)->null).read(new Condition.All(),PageRequest.of(0,10),Set.of("id")));
    }
    @Test void hiddenFieldsCannotBeProjectedFilteredOrSorted(){
        var reader=new JpaQueryReader<>(em,resource,()->(r,q,c)->c.conjunction());
        assertThrows(IllegalArgumentException.class,()->reader.read(new Condition.All(),PageRequest.of(0,10),Set.of("privateNote")));
        assertThrows(IllegalArgumentException.class,()->reader.read(new Condition.Equal("privateNote","secret"),PageRequest.of(0,10),Set.of("id")));
        assertThrows(IllegalArgumentException.class,()->reader.read(new Condition.All(),PageRequest.of(0,10,Sort.by("privateNote")),Set.of("id")));
        assertThrows(IllegalArgumentException.class,()->reader.read(new Condition.All(),Pageable.unpaged(),Set.of("id")));
    }
    @Test void typedNullSemanticsAgreeWithMemoryEvaluation(){
        var reader=new JpaQueryReader<>(em,resource,()->(r,q,c)->c.conjunction());
        Condition notFirst=new Condition.Not(new Condition.Equal("title","first"));
        assertEquals(2,reader.read(notFirst,PageRequest.of(0,10),Set.of("id")).getTotalElements());
        assertThrows(IllegalArgumentException.class,()->reader.read(new Condition.Equal("id","1"),PageRequest.of(0,10),Set.of("id")));
    }
}
