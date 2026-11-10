package cn.sparrowmini.identity.local.autoconfigure;

import cn.sparrowmini.identity.api.*;
import cn.sparrowmini.identity.local.*;
import jakarta.persistence.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.*;
import org.springframework.orm.jpa.*;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.mock.web.MockHttpSession;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

class LocalStarterTest {
    final WebApplicationContextRunner runner=new WebApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(LocalIdentityAutoConfiguration.class,IdentitySelectionAutoConfiguration.class));
    @Configuration(proxyBeanMethods=false)
    static class Database {
        @Bean(destroyMethod="close") EntityManagerFactory entityManagerFactory(){return Persistence.createEntityManagerFactory("starter-test");}
        @Bean EntityManager entityManager(EntityManagerFactory factory){return SharedEntityManagerCreator.createSharedEntityManager(factory);}
        @Bean PlatformTransactionManager transactionManager(EntityManagerFactory factory){return new JpaTransactionManager(factory);}
        @Bean IdentityManagementGuard guard(){return (n,s,id)->{};}
    }
    @Test void localLoginAndPasswordResetInvalidateHttpSession(){
        runner.withUserConfiguration(Database.class).run(context->{
            assertThat(context).hasNotFailed();
            var users=context.getBean(LocalIdentityProvider.class);
            var profile=users.create("alice","Alice","Valid-passphrase-123");
            var mvc=MockMvcBuilders.webAppContextSetup(context.getSourceApplicationContext()).apply(springSecurity()).build();
            var result=mvc.perform(formLogin().user("alice").password("Valid-passphrase-123")).andReturn();
            assertEquals(302,result.getResponse().getStatus());
            var session=(MockHttpSession)result.getRequest().getSession(false);assertNotNull(session);
            assertNotNull(session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY));
            users.resetPassword(profile.id(),profile.version(),"New-passphrase-456");
            assertEquals(401,mvc.perform(get("/protected").session(session)).andReturn().getResponse().getStatus());
            assertTrue(session.isInvalid());
        });
    }
    @Test void externalProviderDoesNotActivateLocalDatabaseOrLogin(){
        UserDirectoryProvider external=new UserDirectoryProvider(){
            public String code(){return "keycloak";}
            public UserProfile get(String id){throw new UnsupportedOperationException();}
            public List<UserProfile> list(String p,int offset,int limit){return List.of();}
        };
        runner.withPropertyValues("sparrow.identity.provider=keycloak","sparrow.identity.authentication=external")
            .withBean("external",UserDirectoryProvider.class,()->external)
            .withBean(ExternalAuthenticationBridge.class,()->()->{})
            .withBean(org.springframework.security.web.SecurityFilterChain.class,()->new org.springframework.security.web.DefaultSecurityFilterChain(request->true,(request,response,chain)->((jakarta.servlet.http.HttpServletResponse)response).sendError(401)))
            .run(context->{
                assertThat(context).hasNotFailed().doesNotHaveBean(LocalIdentityProvider.class).doesNotHaveBean(EntityManagerFactory.class).doesNotHaveBean(FilterChainProxy.class);
                assertSame(external,context.getBean(IdentityProviderRegistry.class).select("keycloak"));
            });
    }
    @Test void missingExternalProviderFailsInsteadOfFallingBack(){
        runner.withPropertyValues("sparrow.identity.provider=keycloak").run(context->assertThat(context).hasFailed());
    }
    @Test void externalDirectoryWithoutAuthenticationBridgeFails(){
        UserDirectoryProvider external=new UserDirectoryProvider(){public String code(){return "external";}public UserProfile get(String id){throw new UnsupportedOperationException();}public List<UserProfile> list(String p,int o,int l){return List.of();}};
        runner.withPropertyValues("sparrow.identity.provider=external").withBean(UserDirectoryProvider.class,()->external)
            .run(context->assertThat(context).hasFailed());
    }
    @Test void unknownAuthenticationModeFails(){runner.withUserConfiguration(Database.class).withPropertyValues("sparrow.identity.authentication=unknown").run(context->assertThat(context).hasFailed());}
}
