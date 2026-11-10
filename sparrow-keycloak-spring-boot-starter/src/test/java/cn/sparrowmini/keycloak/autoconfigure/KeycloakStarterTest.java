package cn.sparrowmini.keycloak.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import cn.sparrowmini.identity.keycloak.*;
import cn.sparrowmini.identity.api.*;
import cn.sparrowmini.rbac.keycloak.*;
import cn.sparrowmini.rbac.core.*;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import static org.assertj.core.api.Assertions.*;
class KeycloakStarterTest {
    ApplicationContextRunner runner(){return new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(KeycloakAutoConfiguration.class)).withPropertyValues("sparrow.identity.provider=keycloak","sparrow.keycloak.issuer=https://identity.test/realms/demo","sparrow.keycloak.audience=backend");}
    @Test void authenticationNeedsNoJpaOrManagementCredentials(){runner().run(c->{assertThat(c).hasNotFailed().hasSingleBean(KeycloakActorProvider.class).hasSingleBean(JwtDecoder.class).doesNotHaveBean(KeycloakAdminClient.class);});}
    @Test void missingAudienceFailsWithoutPermissiveDecoder(){runner().withPropertyValues("sparrow.keycloak.audience=").run(c->assertThat(c).hasFailed());}
    @Test void managementRequiresExplicitGuards(){runner().withPropertyValues("sparrow.keycloak.management-enabled=true","sparrow.keycloak.server-url=https://identity.test","sparrow.keycloak.realm=demo","sparrow.keycloak.management-client-id=service","sparrow.keycloak.management-client-secret=test-only").run(c->assertThat(c).hasFailed());}
    @Test void installsExternalProvidersWithoutLocalAccounts(){runner().withPropertyValues("sparrow.rbac.provider=keycloak","sparrow.keycloak.management-enabled=true","sparrow.keycloak.server-url=https://identity.test","sparrow.keycloak.realm=demo","sparrow.keycloak.management-client-id=service","sparrow.keycloak.management-client-secret=test-only").withBean(IdentityManagementGuard.class,()->(n,s,id)->{}).withBean(RbacManagementGuard.class,()->(n,s,id)->{}).run(c->assertThat(c).hasNotFailed().hasSingleBean(KeycloakUserProvider.class).hasSingleBean(KeycloakRoleProvider.class));}
}
