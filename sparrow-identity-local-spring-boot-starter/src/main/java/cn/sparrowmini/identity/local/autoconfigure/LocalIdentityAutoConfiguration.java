package cn.sparrowmini.identity.local.autoconfigure;

import cn.sparrowmini.identity.api.*;
import cn.sparrowmini.identity.local.*;
import jakarta.persistence.EntityManager;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@AutoConfiguration(after=HibernateJpaAutoConfiguration.class)
@ConditionalOnProperty(prefix="sparrow.identity",name="provider",havingValue="local",matchIfMissing=true)
@EntityScan(basePackageClasses=LocalAccount.class)
@EnableTransactionManagement(proxyTargetClass=true)
public class LocalIdentityAutoConfiguration {
    @Bean @ConditionalOnMissingBean(PasswordEncoder.class)
    PasswordEncoder localPasswordEncoder(){return PasswordEncoderFactories.createDelegatingPasswordEncoder();}
    @Bean
    LocalIdentityProvider localIdentityProvider(EntityManager em,PasswordEncoder encoder,IdentityManagementGuard guard,Environment env){
        return new LocalIdentityProvider(em,encoder,guard,env.getProperty("sparrow.identity.namespace","default"));
    }
    @Configuration(proxyBeanMethods=false)
    @ConditionalOnWebApplication(type=ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnProperty(prefix="sparrow.identity",name="authentication",havingValue="local",matchIfMissing=true)
    @EnableWebSecurity
    static class LocalWebSecurity {
        @Bean @ConditionalOnMissingBean(SecurityFilterChain.class)
        SecurityFilterChain localSecurityFilterChain(HttpSecurity http,LocalIdentityProvider users,PasswordEncoder encoder,Environment env) throws Exception {
            var dao=new DaoAuthenticationProvider(users);dao.setPasswordEncoder(encoder);
            var limited=new LimitedAuthenticationProvider(dao,users,
                env.getProperty("sparrow.identity.login.max-failures",Integer.class,5),
                env.getProperty("sparrow.identity.login.lock-millis",Long.class,900000L),java.time.Clock.systemUTC());
            http.authenticationManager(new org.springframework.security.authentication.ProviderManager(limited))
                .authorizeHttpRequests(authorize->authorize.anyRequest().authenticated())
                .formLogin(form->form.permitAll())
                .logout(logout->logout.permitAll())
                .addFilterAfter(new LocalSessionFilter(users),AnonymousAuthenticationFilter.class);
            // CSRF and session-fixation protection intentionally retain Spring Security defaults.
            return http.build();
        }
    }
}
