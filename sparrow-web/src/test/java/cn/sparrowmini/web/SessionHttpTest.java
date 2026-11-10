package cn.sparrowmini.web;

import cn.sparrowmini.security.api.*;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.web.csrf.*;
import java.util.Set;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SessionHttpTest {
    @Test void reportsOnlyAllowedCapabilitiesAndCsrfToken()throws Exception{
        var read=new PermissionRequest("invoice","read");var edit=new PermissionRequest("invoice","edit");
        var actor=new ActorContext("realm:u1",Set.of("reader"),Set.of());
        var controller=new SessionController(()->actor,(a,p)->new FunctionDecision(a,p,p.equals(read),p.equals(read)?Set.of("reader"):Set.of()),catalog(Set.of(read,edit)));
        var http=MockMvcBuilders.standaloneSetup(controller).build();
        http.perform(get("/api/session").requestAttr(CsrfToken.class.getName(),new DefaultCsrfToken("X-CSRF-TOKEN","_csrf","test-token")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.permissions.length()").value(1))
            .andExpect(jsonPath("$.permissions[0].scope").value("read")).andExpect(jsonPath("$.csrf.token").value("test-token"));
    }
    @Test void statelessHostMayOmitSessionCsrfToken()throws Exception{
        var actor=new ActorContext("realm:u1",Set.of(),Set.of());
        var http=MockMvcBuilders.standaloneSetup(new SessionController(()->actor,(a,p)->new FunctionDecision(a,p,false,Set.of()),catalog(Set.of()))).build();
        http.perform(get("/api/session")).andExpect(status().isOk()).andExpect(jsonPath("$.csrf").isEmpty());
    }
 static PermissionCatalog catalog(Set<PermissionRequest> values){return new PermissionCatalog(){
 public Set<PermissionRequest> permissions(){return values;}
 public boolean exists(PermissionRequest p){return values.contains(p);}
 public boolean isEnabled(PermissionRequest p){return values.contains(p);}
 };}
}
