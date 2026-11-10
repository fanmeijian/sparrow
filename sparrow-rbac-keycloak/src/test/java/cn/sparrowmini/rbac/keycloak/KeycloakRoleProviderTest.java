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
    final TestServer server = new TestServer();
    final KeycloakAdminClient client = new KeycloakAdminClient(server.base(), "demo", "management", "test-secret");
    @AfterEach void close() {
        try { client.close(); } finally { server.close(); }
    }
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


    // Run the SDK against a local HTTP server while retaining the ordered request expectations.
    static final class TestServer implements AutoCloseable {
        private final MockRestServiceServer expectations;
        private final com.sun.net.httpserver.HttpServer http;
        private volatile Throwable failure;
        TestServer() {
            var transport = new RestTemplate();
            expectations = MockRestServiceServer.bindTo(transport).build();
            try {
                http = com.sun.net.httpserver.HttpServer.create(new java.net.InetSocketAddress("127.0.0.1", 0), 0);
            } catch (java.io.IOException e) {
                throw new java.io.UncheckedIOException(e);
            }
            http.createContext("/", exchange -> {
                try {
                    var request = transport.getRequestFactory().createRequest(base().resolve(exchange.getRequestURI()), HttpMethod.valueOf(exchange.getRequestMethod()));
                    exchange.getRequestHeaders().forEach((key, values) -> request.getHeaders().put(key, new ArrayList<>(values)));
                    exchange.getRequestBody().transferTo(request.getBody());
                    try (var response = request.execute()) {
                        byte[] body = response.getBody().readAllBytes();
                        response.getHeaders().forEach((key, values) -> exchange.getResponseHeaders().put(key, new ArrayList<>(values)));
                        exchange.sendResponseHeaders(response.getStatusCode().value(), body.length == 0 ? -1 : body.length);
                        exchange.getResponseBody().write(body);
                    }
                } catch (Exception | AssertionError error) {
                    failure = error;
                    exchange.sendResponseHeaders(500, -1);
                } finally {
                    exchange.close();
                }
            });
            http.start();
        }
        URI base() { return URI.create("http://127.0.0.1:" + http.getAddress().getPort()); }
        org.springframework.test.web.client.ResponseActions expect(org.springframework.test.web.client.RequestMatcher matcher) {
            return expectations.expect(matcher);
        }
        void verify() {
            if (failure != null) throw new AssertionError(failure);
            expectations.verify();
        }
        @Override public void close() { http.stop(0); }
    }
}
