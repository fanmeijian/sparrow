package cn.sparrowmini.rbac.autoconfigure;

import cn.sparrowmini.rbac.core.*;
import cn.sparrowmini.security.api.FunctionPermissionEvaluator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.SmartInitializingSingleton;
@AutoConfiguration(after={LocalRbacAutoConfiguration.class,ExternalRoleGrantAutoConfiguration.class})
public class RbacSelectionAutoConfiguration {
    @Bean SmartInitializingSingleton validateRoleProvider(RoleDirectoryProvider directory,RoleAssignmentProvider assignments,RolePermissionManager grants,FunctionPermissionEvaluator evaluator,Environment env){
        return ()->{if(!env.getProperty("sparrow.rbac.provider","local").equals(directory.code()))throw new IllegalStateException("Configured role provider is not installed");};
    }
}
