package cn.sparrowmini.rbac.keycloak;

import org.junit.jupiter.api.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.http.*;
import java.net.URI;
import java.util.*;
import cn.sparrowmini.identity.keycloak.*;
import cn.sparrowmini.security.api.Subject;
import cn.sparrowmini.rbac.core.RoleCapability;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
class KeycloakRoleProviderTest {
    final String issuer="https://identity.test/realms/demo";
    final RestTemplate http=new RestTemplate();final MockRestServiceServer server=MockRestServiceServer.bindTo(http).build();
    final KeycloakAdminClient client=new KeycloakAdminClient(http,URI.create("https://identity.test"),"demo","management","test-secret");
    final KeycloakRoleProvider roles=new KeycloakRoleProvider(client,issuer,"demo",null,(n,s,id)->{});
    final Subject user=new Subject(Subject.Kind.USER,KeycloakActorProvider.subject(issuer,"user-1"));
    void token(){server.expect(r->assertEquals("/realms/demo/protocol/openid-connect/token",r.getURI().getPath())).andRespond(withSuccess("{\"access_token\":\"test-token\",\"expires_in\":300}",MediaType.APPLICATION_JSON));}
    void role(){server.expect(r->assertEquals("/admin/realms/demo/roles-by-id/role-1",r.getURI().getPath())).andRespond(withSuccess("{\"id\":\"role-1\",\"name\":\"reader\",\"clientRole\":false}",MediaType.APPLICATION_JSON));}
    @Test void readsEffectiveRolesAgainAfterRevocation(){
        token();server.expect(r->assertEquals("/admin/realms/demo/users/user-1",r.getURI().getPath())).andRespond(withSuccess("{\"enabled\":true}",MediaType.APPLICATION_JSON));server.expect(r->assertEquals("/admin/realms/demo/users/user-1/role-mappings/realm/composite",r.getURI().getPath())).andRespond(withSuccess("[{\"id\":\"role-1\",\"name\":\"reader\",\"clientRole\":false}]",MediaType.APPLICATION_JSON));
        server.expect(r->{}).andRespond(withSuccess("{\"enabled\":true}",MediaType.APPLICATION_JSON));
        server.expect(r->{}).andRespond(withSuccess("[]",MediaType.APPLICATION_JSON));
        assertEquals(Set.of(roles.roleId("role-1")),roles.rolesFor(user));assertTrue(roles.rolesFor(user).isEmpty());server.verify();
    }
    @Test void assignsOnlyInsideConfiguredCatalog(){token();role();server.expect(r->{assertEquals(HttpMethod.POST,r.getMethod());assertEquals("/admin/realms/demo/users/user-1/role-mappings/realm",r.getURI().getPath());}).andRespond(withNoContent());roles.assign(roles.roleId("role-1"),user);server.verify();}
    @Test void rejectsClientRoleInRealmCatalog(){token();server.expect(r->{}).andRespond(withSuccess("{\"id\":\"role-1\",\"name\":\"reader\",\"clientRole\":true,\"containerId\":\"other-client\"}",MediaType.APPLICATION_JSON));assertThrows(IllegalArgumentException.class,()->roles.assign(roles.roleId("role-1"),user));server.verify();}
    @Test void unsupportedRoleStateAndDeniedAssignmentMakeNoRequest(){
        assertFalse(roles.capabilities().contains(RoleCapability.SET_ENABLED));assertThrows(IllegalArgumentException.class,()->roles.setEnabled(roles.roleId("role-1"),-1,false));
        var denied=new KeycloakRoleProvider(client,issuer,"demo",null,(n,s,id)->{throw new SecurityException();});assertThrows(SecurityException.class,()->denied.assign(roles.roleId("role-1"),user));server.verify();
    }
    @Test void listsMembersThroughRoleNameEndpoints(){token();role();
        server.expect(r->assertEquals("/admin/realms/demo/roles/reader/users",r.getURI().getPath())).andRespond(withSuccess("[{\"id\":\"user-1\"}]",MediaType.APPLICATION_JSON));
        server.expect(r->assertEquals("/admin/realms/demo/roles/reader/groups",r.getURI().getPath())).andRespond(withSuccess("[]",MediaType.APPLICATION_JSON));
        assertEquals(Set.of(user),roles.members(roles.roleId("role-1")));server.verify();
    }
    @Test void disabledUserHasNoEffectiveRoles(){token();server.expect(r->{}).andRespond(withSuccess("{\"enabled\":false}",MediaType.APPLICATION_JSON));assertTrue(roles.rolesFor(user).isEmpty());server.verify();}

}
