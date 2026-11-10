package cn.sparrowmini.rbac.autoconfigure;

import cn.sparrowmini.rbac.core.*;
import cn.sparrowmini.rbac.persistence.jpa.grant.*;
import cn.sparrowmini.security.api.*;
import jakarta.persistence.EntityManager;
import org.springframework.boot.autoconfigure.*;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import java.util.Set;
@AutoConfiguration(after=HibernateJpaAutoConfiguration.class)
@Conditional(ExternalRoleCondition.class)
@ConditionalOnBean({EntityManager.class,RoleReferenceValidator.class,GrantManagementGuard.class})
@ConditionalOnMissingBean(RolePermissionManager.class)
@EntityScan(basePackageClasses={RolePermissionEntity.class,cn.sparrowmini.rbac.persistence.jpa.catalog.PermissionResourceEntity.class})
@EnableTransactionManagement(proxyTargetClass=true)
public class ExternalRoleGrantAutoConfiguration {
    @Bean JpaRoleGrantStore externalRoleGrants(EntityManager em,RoleReferenceValidator roles,GrantManagementGuard guard,PermissionCatalog registered,Environment env){return new JpaRoleGrantStore(em,env.getProperty("sparrow.rbac.namespace",env.getProperty("sparrow.identity.namespace","default")),registered,roles,guard);}
    @Bean @ConditionalOnMissingBean(FunctionPermissionEvaluator.class)
    FunctionPermissionEvaluator externalRoleEvaluator(RoleAssignmentProvider roles,RoleGrantSource grants,PermissionCatalog registered){return new AssignedRolePermissionEvaluator(roles,grants,registered);}
}
