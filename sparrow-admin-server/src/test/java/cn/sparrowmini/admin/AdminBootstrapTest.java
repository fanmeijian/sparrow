package cn.sparrowmini.admin;

import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.WebApplicationType;
import org.springframework.mock.env.MockEnvironment;
import jakarta.persistence.EntityManagerFactory;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class AdminBootstrapTest {
    @Test void bootstrapRequiresAnExplicitOfflineProcess(){
        var env=new MockEnvironment().withProperty("spring.main.web-application-type","servlet");
        assertThrows(IllegalStateException.class,()->new AdminBootstrap().initializeAdmin(env,null,null,null,null,null,null,null,null));
    }
    @Test void offlineBootstrapIsAuditedAndCannotBeRepeated(){
        String database="bootstrap-"+UUID.randomUUID();
        String[] base={"--spring.main.web-application-type=none","--sparrow.bootstrap.enabled=true","--sparrow.bootstrap.username=administrator","--sparrow.bootstrap.password=Initial-passphrase-123",
            "--spring.datasource.url=jdbc:h2:mem:"+database+";DB_CLOSE_DELAY=-1","--spring.datasource.username=sa","--spring.datasource.password=","--sparrow.storage.local.root="+System.getProperty("java.io.tmpdir")+"/"+database,"--spring.main.banner-mode=off"};
        var first=new ArrayList<>(List.of(base));first.add("--spring.jpa.hibernate.ddl-auto=create");
        try(var context=new SpringApplicationBuilder(AdminApplication.class).web(WebApplicationType.NONE).run(first.toArray(String[]::new))){
            try(var em=context.getBean(EntityManagerFactory.class).createEntityManager()){
                var cb=em.getCriteriaBuilder();var q=cb.createQuery(Long.class);var r=q.from(cn.sparrowmini.system.audit.AuditEvent.class);
                q.select(cb.count(r)).where(cb.equal(r.get("operation"),"BOOTSTRAP"));assertEquals(1L,em.createQuery(q).getSingleResult());
            }
        }
        var second=new ArrayList<>(List.of(base));second.add("--spring.jpa.hibernate.ddl-auto=validate");
        assertThrows(IllegalStateException.class,()->{
            try(var context=new SpringApplicationBuilder(AdminApplication.class).web(WebApplicationType.NONE).run(second.toArray(String[]::new))){fail("Repeated bootstrap must fail");}
        });
    }
}
