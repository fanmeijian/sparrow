package cn.sparrowmini.identity.keycloak;

import org.junit.jupiter.api.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.http.*;
import java.net.URI;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
class KeycloakUserProviderTest {
    final String issuer="https://identity.test/realms/demo";
    final TestServer server = new TestServer();
    final KeycloakAdminClient client = new KeycloakAdminClient(server.base(), "demo", "management", "test-secret");
    @AfterEach void close() {
        try { client.close(); } finally { server.close(); }
    }
    final KeycloakUserProvider users=new KeycloakUserProvider(client,issuer,"demo",(n,s,id)->{});
    String subject(){return KeycloakActorProvider.subject(issuer,"user-1");}
    void token(){server.expect(request->{assertEquals("/realms/demo/protocol/openid-connect/token",request.getURI().getPath());assertEquals(HttpMethod.POST,request.getMethod());}).andRespond(withSuccess("{\"access_token\":\"test-token\",\"expires_in\":300}",MediaType.APPLICATION_JSON));}
    void user(){server.expect(request->{assertEquals("/admin/realms/demo/users/user-1",request.getURI().getPath());assertEquals("Bearer test-token",request.getHeaders().getFirst("Authorization"));}).andRespond(withSuccess("{\"id\":\"user-1\",\"username\":\"alice\",\"firstName\":\"Alice\",\"enabled\":true}",MediaType.APPLICATION_JSON));}
    @Test void readsIssuerQualifiedIdentityAndReusesServiceToken(){token();user();user();var result=users.get(subject());assertEquals(subject(),result.id());assertEquals(-1,result.version());assertEquals("alice",users.get(subject()).username());server.verify();}
    @Test void guardAndVersionRejectionsDoNotCallRemoteServer(){
        var denied=new KeycloakUserProvider(client,issuer,"demo",(n,s,id)->{throw new SecurityException("denied");});
        assertThrows(SecurityException.class,()->denied.resetPassword(subject(),-1,"new-password"));
        assertThrows(IllegalArgumentException.class,()->users.update(subject(),0,"new-name"));
        assertThrows(IllegalArgumentException.class,()->users.get(KeycloakActorProvider.subject("https://other.test","user-1")));
        server.verify();
    }
    @Test void passwordResetUsesDedicatedOperation(){token();server.expect(request->{assertEquals(HttpMethod.PUT,request.getMethod());assertEquals("/admin/realms/demo/users/user-1/reset-password",request.getURI().getPath());}).andRespond(withNoContent());users.resetPassword(subject(),-1,"new-password");server.verify();}
    @Test void upstreamFailureDoesNotExposeCredentialsOrResponse(){
        server.expect(request->{}).andRespond(withStatus(HttpStatus.UNAUTHORIZED).body("test-secret"));
        var error=assertThrows(KeycloakAdminClient.UpstreamException.class,()->users.get(subject()));assertTrue(error.getMessage().contains("HTTP 401"));assertFalse(error.toString().contains("test-secret"));assertNull(error.getCause());server.verify();
    }
    @Test void createsDisabledAccountBeforeRoleAssignmentWorkflow(){
        token();server.expect(request->{
            assertEquals(HttpMethod.POST,request.getMethod());
            assertEquals("/admin/realms/demo/users",request.getURI().getPath());
            String body=((org.springframework.mock.http.client.MockClientHttpRequest)request).getBodyAsString();
            assertTrue(body.contains("\"enabled\":false"));
        }).andRespond(withCreatedEntity(URI.create("https://identity.test/admin/realms/demo/users/user-1")));
        server.expect(request->{}).andRespond(withSuccess("{\"id\":\"user-1\",\"username\":\"alice\",\"enabled\":false}",MediaType.APPLICATION_JSON));
        assertFalse(users.createDisabled("alice","Alice","new-password").enabled());server.verify();
    }


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
