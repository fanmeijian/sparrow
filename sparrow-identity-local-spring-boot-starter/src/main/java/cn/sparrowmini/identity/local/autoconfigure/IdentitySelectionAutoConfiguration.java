package cn.sparrowmini.identity.local.autoconfigure;

import cn.sparrowmini.identity.api.*;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import java.util.List;

@AutoConfiguration(after=LocalIdentityAutoConfiguration.class)
public class IdentitySelectionAutoConfiguration {
    @Bean IdentityProviderRegistry identityProviderRegistry(List<UserDirectoryProvider> providers,Environment environment){
        var registry=new IdentityProviderRegistry(providers);
        registry.select(environment.getProperty("sparrow.identity.provider","local"));
        return registry;
    }
}
