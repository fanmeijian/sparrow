package cn.sparrowmini.rbac.autoconfigure;

import cn.sparrowmini.rbac.core.*;
import cn.sparrowmini.rbac.persistence.jpa.JpaRoleProvider;
import cn.sparrowmini.security.api.PermissionCatalog;
import jakarta.persistence.EntityManager;
import org.springframework.boot.autoconfigure.*;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import java.util.Set;
@AutoConfiguration(after=HibernateJpaAutoConfiguration.class)
@ConditionalOnProperty(name="sparrow.rbac.provider",havingValue="local",matchIfMissing=true)
@EntityScan(basePackageClasses=JpaRoleProvider.class)
@EnableTransactionManagement(proxyTargetClass=true)
public class LocalRbacAutoConfiguration {
    @Bean @ConditionalOnMissingBean(RoleDirectoryProvider.class)
    JpaRoleProvider localRoles(EntityManager em,RbacManagementGuard guard,PermissionCatalog registered,Environment env){return new JpaRoleProvider(em,env.getProperty("sparrow.rbac.namespace",env.getProperty("sparrow.identity.namespace","default")),guard,registered);}
}
