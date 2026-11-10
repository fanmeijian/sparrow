package cn.sparrowmini.datapolicy.autoconfigure;

import cn.sparrowmini.datapolicy.persistence.jpa.*;
import cn.sparrowmini.datapolicy.core.DataRule;
import cn.sparrowmini.datapolicy.core.Condition;
import cn.sparrowmini.security.api.*;
import jakarta.persistence.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.*;
import org.springframework.orm.jpa.*;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.data.domain.PageRequest;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
class DataPolicyStarterTest {
    static final PermissionRequest READ=new PermissionRequest("record","read");
    @Entity @Table(name="sample_policy_record") public static class Record {
        @Id String id;String owner;
        public Record() { }
        Record(String id,String owner){this.id=id;this.owner=owner;}
    }
    @Configuration(proxyBeanMethods=false) static class Database {
        @Bean(destroyMethod="close") EntityManagerFactory entityManagerFactory(){return Persistence.createEntityManagerFactory("policy-starter-test");}
        @Bean EntityManager entityManager(EntityManagerFactory factory){return SharedEntityManagerCreator.createSharedEntityManager(factory);}
        @Bean PlatformTransactionManager transactionManager(EntityManagerFactory factory){return new JpaTransactionManager(factory);}
        @Bean JpaPolicyStore.Guard guard(){return (n,op,p)->{};}
        @Bean DataPolicyRegistration registration(){return new DataPolicyRegistration(Map.of(READ,new JpaResource<>("record",Record.class,"id",Set.of("id","owner"))),Map.of(READ,Set.of("owner")));}
    }
    @Configuration(proxyBeanMethods=false) static class Functions {
        @Bean FunctionPermissionEvaluator evaluator(){return (actor,permission)->new FunctionDecision(actor,permission,permission.equals(READ),Set.of());}
    }
    @Configuration(proxyBeanMethods=false) static class Extension {
        @Bean DataPolicyExtension owner(){return new DataPolicyExtension(){public String code(){return "owner";}public Condition restrict(ActorContext actor,PermissionRequest permission){return new Condition.Equal("owner",actor.subject());}};}
    }
    final ApplicationContextRunner runner=new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(DataPolicyAutoConfiguration.class)).withUserConfiguration(Database.class);
    @Test void customFunctionProviderAndRequiredExtensionWorkWithoutRbac(){runner.withUserConfiguration(Functions.class,Extension.class).run(c->{
        assertThat(c).hasNotFailed();var em=c.getBean(EntityManager.class);var tx=new TransactionTemplate(c.getBean(PlatformTransactionManager.class));
        tx.executeWithoutResult(status->{em.persist(new Record("one","alice"));em.persist(new Record("two","bob"));});
        var store=c.getBean(JpaPolicyStore.class);var subject=new Subject(Subject.Kind.USER,"alice");
        var rules=List.of(new DataRule(subject,READ,Effect.ALLOW,new Condition.All(),DataRule.Target.ROW,Set.of()),new DataRule(subject,READ,Effect.ALLOW,new Condition.All(),DataRule.Target.FIELD_READ,Set.of("id","owner")));
        var draft=store.saveDraft(READ,null,rules);store.publish(READ,draft.version());
        var plans=c.getBean(DataPlanFactory.class);var page=plans.read(new ActorContext("alice",Set.of(),Set.of()),READ,new Condition.All(),PageRequest.of(0,1),Set.of("id"));
        assertEquals(1,page.getTotalElements());assertEquals("one",page.getContent().get(0).get("id"));
        store.retire(READ,store.draft(READ).version());assertEquals(0,plans.read(new ActorContext("alice",Set.of(),Set.of()),READ,new Condition.All(),PageRequest.of(0,1),Set.of("id")).getTotalElements());
    });}
    @Test void missingRequiredExtensionFailsAtStartup(){runner.withUserConfiguration(Functions.class).run(c->assertThat(c).hasFailed());}
    @Test void missingFunctionalAuthorizationFailsAtStartup(){runner.withUserConfiguration(Extension.class).run(c->assertThat(c).hasFailed());}
    @Test void disabledModuleDoesNotCreatePolicyBeans(){runner.withPropertyValues("sparrow.data-policy.enabled=false").run(c->assertThat(c).hasNotFailed().doesNotHaveBean(JpaPolicyStore.class).doesNotHaveBean(DataPlanFactory.class));}
}
