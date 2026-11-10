package cn.sparrowmini.web;

import cn.sparrowmini.identity.api.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import static org.assertj.core.api.Assertions.assertThat;

class ManagementConfigurationTest {
    final WebApplicationContextRunner runner=new WebApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(ManagementWebConfiguration.class));
    @Test void disabledByDefault(){runner.run(c->assertThat(c).doesNotHaveBean(IdentityController.class));}
    @Test void onlyInstalledServicesExposeEndpoints(){
        runner.withPropertyValues("sparrow.management.enabled=true")
            .withBean(UserDirectoryProvider.class,IdentityHttpTest.Directory::new)
            .run(c->{assertThat(c).hasSingleBean(IdentityController.class);assertThat(c).doesNotHaveBean(RoleController.class);assertThat(c).doesNotHaveBean(PolicyController.class);});
    }
    @Test void identityOnlyHostDoesNotRequireOptionalFeatureClasses(){
        runner.withPropertyValues("sparrow.management.enabled=true")
            .withClassLoader(new FilteredClassLoader("cn.sparrowmini.datapolicy","cn.sparrowmini.group","cn.sparrowmini.system","cn.sparrowmini.file","cn.sparrowmini.rbac","org.hibernate","org.springframework.data.jpa"))
            .withBean(UserDirectoryProvider.class,IdentityHttpTest.Directory::new)
            .run(c->{assertThat(c).hasNotFailed();assertThat(c).hasSingleBean(IdentityController.class);});
    }
}
