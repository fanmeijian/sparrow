package cn.sparrowmini.identity.local.autoconfigure;

import cn.sparrowmini.identity.api.*;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import java.util.List;

@AutoConfiguration(after=LocalIdentityAutoConfiguration.class)
public class IdentitySelectionAutoConfiguration {
    @Bean IdentityProviderRegistry identityProviderRegistry(List<UserDirectoryProvider> providers,Environment environment){
        var registry=new IdentityProviderRegistry(providers);
        String selected=environment.getProperty("sparrow.identity.provider","local");
        String mode=environment.getProperty("sparrow.identity.authentication",selected.equals("local")?"local":"external");
        if(!java.util.Set.of("local","external").contains(mode))throw new IllegalStateException("Unknown authentication mode");
        if(mode.equals("local")&&!selected.equals("local"))throw new IllegalStateException("Local authentication requires local identity provider");
        registry.select(selected);
        return registry;
    }
    @Configuration(proxyBeanMethods=false)
    @org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication(type=org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET)
    @Conditional(ExternalAuthenticationCondition.class)
    static class ExternalAuthentication {
        @Bean org.springframework.beans.factory.SmartInitializingSingleton requireExternalAuthentication(
            org.springframework.beans.factory.ObjectProvider<ExternalAuthenticationBridge> bridges,
            org.springframework.beans.factory.ObjectProvider<org.springframework.security.web.SecurityFilterChain> chains){
            return ()->{var installed=bridges.orderedStream().toList();
                if(installed.size()!=1||chains.orderedStream().findAny().isEmpty())throw new IllegalStateException("External authentication requires an explicit bridge and security filter chain");
                installed.get(0).validateConfiguration();
            };
        }
    }
}
