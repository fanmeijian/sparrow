package cn.sparrowmini.web;

import cn.sparrowmini.rbac.core.*;
import cn.sparrowmini.security.api.*;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RoleHttpTest {
    static class ExternalRoles implements RoleDirectoryProvider,RoleAssignmentProvider,RolePermissionManager {
        String assigned;PermissionRequest granted;
        public Set<Subject> members(String id){return Set.of(new Subject(Subject.Kind.USER,"realm:u1"));}
        public List<RoleGrant> grants(String id){return List.of(new RoleGrant(id,new PermissionRequest("invoice","read"),Effect.ALLOW));}
        public String code(){return "external";}
        public List<RoleProfile> list(int offset,int limit){return List.of(new RoleProfile("realm:editor","editor","Editor",true,1));}
        public RoleProfile create(String code,String name){throw new UnsupportedOperationException();}
        public RoleProfile setEnabled(String id,long version,boolean enabled){throw new UnsupportedOperationException();}
        public Set<String> rolesFor(Subject subject){return Set.of();}
        public void assign(String role,Subject subject){assigned=role+"/"+subject.id();}
        public void revoke(String role,Subject subject){throw new SecurityException();}
        public void grant(String role,PermissionRequest request,Effect effect){granted=request;}
        public void revokePermission(String role,PermissionRequest request){throw new SecurityException();}
    }
    @Test void externalRoleProviderManagesMembershipWithoutJpa()throws Exception{
        var roles=new ExternalRoles();var http=MockMvcBuilders.standaloneSetup(new RoleController(roles,roles,roles)).setControllerAdvice(new ApiErrors()).build();
        http.perform(get("/api/admin/roles")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value("realm:editor"));
        http.perform(post("/api/admin/roles/realm:editor/members").contentType(MediaType.APPLICATION_JSON)
            .content("{\"kind\":\"USER\",\"subject\":\"realm:u1\"}")).andExpect(status().isOk());
        assertEquals("realm:editor/realm:u1",roles.assigned);
        http.perform(get("/api/admin/roles/realm:editor/members")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value("realm:u1"));
        http.perform(get("/api/admin/roles/realm:editor/permissions")).andExpect(status().isOk()).andExpect(jsonPath("$[0].permission.scope").value("read"));
    }
    @Test void functionalGrantPassesExactScopeToIndependentManager()throws Exception{
        var roles=new ExternalRoles();var http=MockMvcBuilders.standaloneSetup(new RoleController(roles,roles,roles)).setControllerAdvice(new ApiErrors()).build();
        http.perform(put("/api/admin/roles/realm:editor/permissions").contentType(MediaType.APPLICATION_JSON)
            .content("{\"resource\":\"invoice\",\"scope\":\"approve\",\"effect\":\"ALLOW\"}")).andExpect(status().isOk());
        assertEquals(new PermissionRequest("invoice","approve"),roles.granted);
        http.perform(delete("/api/admin/roles/realm:editor/members").contentType(MediaType.APPLICATION_JSON)
            .content("{\"kind\":\"USER\",\"subject\":\"realm:u1\"}")).andExpect(status().isForbidden());
    }
}
