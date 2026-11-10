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
    final RestTemplate http=new RestTemplate();
    final MockRestServiceServer server=MockRestServiceServer.bindTo(http).build();
    final KeycloakAdminClient client=new KeycloakAdminClient(http,URI.create("https://identity.test"),"demo","management","test-secret");
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
        var error=assertThrows(IllegalStateException.class,()->users.get(subject()));assertFalse(error.toString().contains("test-secret"));assertNull(error.getCause());server.verify();
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

}
