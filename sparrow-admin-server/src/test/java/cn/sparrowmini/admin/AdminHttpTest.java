package cn.sparrowmini.admin;

import cn.sparrowmini.identity.local.*;
import cn.sparrowmini.rbac.persistence.jpa.*;
import cn.sparrowmini.group.persistence.jpa.*;
import cn.sparrowmini.security.api.*;
import cn.sparrowmini.identity.api.UserProfile;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.http.MediaType;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:admin-http;DB_CLOSE_DELAY=-1","spring.datasource.username=sa","spring.datasource.password=","spring.jpa.hibernate.ddl-auto=create-drop"})
@AutoConfigureMockMvc
class AdminHttpTest {
    @DynamicPropertySource static void properties(DynamicPropertyRegistry registry){registry.add("sparrow.storage.local.root",()->System.getProperty("java.io.tmpdir")+"/sparrow-admin-http-"+UUID.randomUUID());}
    @Autowired MockMvc http;
    @Autowired AdminAccess access;
    @Autowired LocalIdentityProvider users;
    @Autowired JpaRoleProvider roles;
    @Autowired JpaGroupService groups;
    @Autowired Set<PermissionRequest> registered;
    UserProfile admin,reader;String adminRole;
    static final String PASSWORD="Valid-passphrase-123";
    @BeforeEach void seed(){access.initialize(()->{
        groups.initialize();String suffix=UUID.randomUUID().toString().substring(0,8);
        admin=users.create("admin-"+suffix,"Admin",PASSWORD);reader=users.create("reader-"+suffix,"Reader",PASSWORD);
        var role=roles.create("admin-"+suffix,"Admin");adminRole=role.id();
        roles.assign(adminRole,new Subject(Subject.Kind.USER,"default:"+admin.id()));
        registered.stream().filter(p->p.resource().startsWith("admin.")).forEach(p->roles.grant(adminRole,p,Effect.ALLOW));
    });}
    MockHttpSession login(UserProfile user)throws Exception{
        var result=http.perform(formLogin().user(user.username()).password(PASSWORD)).andExpect(status().is3xxRedirection()).andReturn();
        return (MockHttpSession)result.getRequest().getSession(false);
    }
    @Test void anonymousAndUnprivilegedUserCannotManageAccounts()throws Exception{
        http.perform(get("/api/admin/users")).andExpect(status().is3xxRedirection());
        var session=login(reader);
        http.perform(get("/api/admin/users").session(session)).andExpect(status().isForbidden());
        http.perform(post("/api/admin/users").session(session).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"intruder\",\"displayName\":\"Intruder\",\"password\":\"Valid-passphrase-123\"}"))
            .andExpect(status().isForbidden());
    }
    @Test void adminCanManageButCsrfIsRequired()throws Exception{
        var session=login(admin);
        http.perform(get("/api/session").session(session)).andExpect(status().isOk()).andExpect(jsonPath("$.csrf.token").isNotEmpty());
        http.perform(get("/api/admin/users").session(session)).andExpect(status().isOk());
        String body="{\"version\":"+reader.version()+",\"displayName\":\"Updated\"}";
        http.perform(put("/api/admin/users/"+reader.id()).session(session).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isForbidden());
        http.perform(put("/api/admin/users/"+reader.id()).session(session).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk()).andExpect(jsonPath("$.displayName").value("Updated"));
        http.perform(put("/api/admin/users/"+reader.id()).session(session).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isConflict());
    }
    @Test void membershipRevocationAppliesToExistingSession()throws Exception{
        var session=login(admin);
        http.perform(get("/api/admin/users").session(session)).andExpect(status().isOk());
        access.initialize(()->roles.revoke(adminRole,new Subject(Subject.Kind.USER,"default:"+admin.id())));
        http.perform(get("/api/admin/users").session(session)).andExpect(status().isForbidden());
    }
    @Test void passwordResetInvalidatesExistingSession()throws Exception{
        var session=login(reader);
        access.initialize(()->users.resetPassword(reader.id(),reader.version(),"Changed-passphrase-456"));
        http.perform(get("/api/session").session(session)).andExpect(status().isUnauthorized());assertTrue(session.isInvalid());
    }

    @Test void policyDraftUsesNullableCreationVersionAndRejectsStaleUpdates()throws Exception{
        var session=login(admin);String path="/api/admin/policies?resource=file.object&scope=read";
        var mapper=new com.fasterxml.jackson.databind.ObjectMapper();
        var current=http.perform(get(path).session(session)).andExpect(status().isOk()).andReturn();
        var version=mapper.readTree(current.getResponse().getContentAsString()).get("version");
        String body="{\"version\":"+version+",\"document\":\"[]\"}";
        var saved=http.perform(put(path).session(session).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isOk()).andReturn();
        long savedVersion=mapper.readTree(saved.getResponse().getContentAsString()).get("version").asLong();
        http.perform(post("/api/admin/policies/publish?resource=file.object&scope=read").session(session).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"version\":"+savedVersion+"}")).andExpect(status().isOk());
        http.perform(put(path).session(session).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"version\":"+savedVersion+",\"document\":\"[]\"}")).andExpect(status().isConflict());
    }
    @Test void managementAssetsAreServedOnlyAfterLogin()throws Exception{
        http.perform(get("/sparrow-admin/index.html")).andExpect(status().is3xxRedirection());
        var session=login(admin);
        http.perform(get("/sparrow-admin/index.html").session(session)).andExpect(status().isOk());
        http.perform(get("/sparrow-admin/admin.js").session(session)).andExpect(status().isOk());
    }
}
