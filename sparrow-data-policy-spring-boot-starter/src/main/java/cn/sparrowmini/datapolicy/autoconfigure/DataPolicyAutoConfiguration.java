package cn.sparrowmini.datapolicy.autoconfigure;

import cn.sparrowmini.datapolicy.persistence.jpa.*;
import cn.sparrowmini.security.api.FunctionPermissionEvaluator;
import jakarta.persistence.EntityManager;
import org.springframework.boot.autoconfigure.*;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.transaction.annotation.EnableTransactionManagement;
@AutoConfiguration(after=HibernateJpaAutoConfiguration.class,afterName="cn.sparrowmini.rbac.autoconfigure.RbacSelectionAutoConfiguration")
@ConditionalOnProperty(name="sparrow.data-policy.enabled",havingValue="true",matchIfMissing=true)
@EntityScan(basePackageClasses=PolicyDocument.class)
@EnableTransactionManagement(proxyTargetClass=true)
public class DataPolicyAutoConfiguration {
    @Bean @ConditionalOnMissingBean(JpaPolicyStore.class)
    JpaPolicyStore policyStore(EntityManager em,JpaPolicyStore.Guard guard,DataPolicyRegistration registration,Environment env,FunctionPermissionEvaluator functions){return new JpaPolicyStore(em,env.getProperty("sparrow.data-policy.namespace",env.getProperty("sparrow.identity.namespace","default")),guard,registration.resources(),functions.supportsRoleEvidence());}
    @Bean @ConditionalOnMissingBean(DataPlanFactory.class)
    DataPlanFactory dataPlans(EntityManager em,FunctionPermissionEvaluator functions,JpaPolicyStore policies,DataPolicyRegistration registration,ObjectProvider<DataPolicyExtension> extensions){return new DataPlanFactory(em,functions,policies,registration,extensions.orderedStream().toList());}
}
