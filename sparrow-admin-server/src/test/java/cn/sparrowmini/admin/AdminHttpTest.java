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

    @Test void lockedAccountCannotFallBackToAnotherAuthenticationProvider()throws Exception{
        for(int i=0;i<5;i++)http.perform(formLogin().user(reader.username()).password("incorrect")).andExpect(redirectedUrl("/login?error"));
        http.perform(formLogin().user(reader.username()).password(PASSWORD)).andExpect(redirectedUrl("/login?error"));
        access.initialize(()->{var current=users.get(reader.id());users.resetPassword(reader.id(),current.version(),PASSWORD);});
        assertNotNull(login(reader));
    }

    @Autowired cn.sparrowmini.web.IdentityController userCommands;
    @Test void auditRecordsOutcomesWithoutPasswords()throws Exception{
        var session=login(admin);
        http.perform(put("/api/admin/users/"+reader.id()).session(session).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content("{\"version\":"+reader.version()+",\"displayName\":\"Audit update\"}")).andExpect(status().isOk());
        var result=http.perform(get("/api/admin/audit").session(session)).andExpect(status().isOk()).andReturn();
        String json=result.getResponse().getContentAsString();assertTrue(json.contains("SUCCEEDED"));assertFalse(json.contains(PASSWORD));assertFalse(json.contains("passwordHash"));assertFalse(json.contains("Audit update"));
        http.perform(get("/api/admin/audit").session(login(reader))).andExpect(status().isForbidden());
    }
    @Test void auditFailureRollsBackBusinessMutation(){
        var request=new org.springframework.mock.web.MockHttpServletRequest("POST","/api/admin/"+"x".repeat(600));
        org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(new org.springframework.web.context.request.ServletRequestAttributes(request));
        var principal=users.loadUserByUsername(admin.username());
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated(principal,null,List.of()));
        String username="rollback-"+UUID.randomUUID();
        try{assertThrows(IllegalArgumentException.class,()->userCommands.create(new cn.sparrowmini.web.IdentityController.Create(username,"Rollback",PASSWORD)));}
        finally{org.springframework.security.core.context.SecurityContextHolder.clearContext();org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();}
        access.initialize(()->assertTrue(users.list(username,0,50).isEmpty()));
    }

    @Autowired cn.sparrowmini.file.FileService fileAdministration;
    @Autowired cn.sparrowmini.datapolicy.persistence.jpa.JpaPolicyStore policies;
    @Autowired jakarta.persistence.EntityManager em;
    @Autowired org.springframework.transaction.PlatformTransactionManager transactions;
    cn.sparrowmini.file.FileService.Info sampleFile(String name){
        var result=new java.util.concurrent.atomic.AtomicReference<cn.sparrowmini.file.FileService.Info>();
        access.initialize(()->{try{result.set(fileAdministration.upload("local",name,new java.io.ByteArrayInputStream("content".getBytes()),null));}catch(java.io.IOException e){throw new java.io.UncheckedIOException(e);}});return result.get();
    }
    String businessRole(String... scopes){
        var result=new java.util.concurrent.atomic.AtomicReference<String>();access.initialize(()->{
            var role=roles.create("file-"+UUID.randomUUID(),"File access");result.set(role.id());roles.assign(role.id(),new Subject(Subject.Kind.USER,"default:"+reader.id()));
            for(String scope:scopes)roles.grant(role.id(),new PermissionRequest("file.object",scope),Effect.ALLOW);
        });return result.get();
    }
    void publish(String scope,List<cn.sparrowmini.datapolicy.core.DataRule> rules){access.initialize(()->{
        var permission=new PermissionRequest("file.object",scope);Long version;
        try{version=policies.draft(permission).version();}catch(NoSuchElementException missing){version=null;}
        var saved=policies.saveDraft(permission,version,rules);policies.publish(permission,saved.version());
    });}
    cn.sparrowmini.datapolicy.core.DataRule rule(String role,String scope,cn.sparrowmini.datapolicy.core.Condition condition,cn.sparrowmini.datapolicy.core.DataRule.Target target,Set<String> fields){
        return new cn.sparrowmini.datapolicy.core.DataRule(new Subject(Subject.Kind.ROLE,role),new PermissionRequest("file.object",scope),Effect.ALLOW,condition,target,fields);
    }
    @Test void fileRowsFieldsAndDirectDownloadsUseTheSamePolicy()throws Exception{
        var one=sampleFile("one.txt");var two=sampleFile("two.txt");String role=businessRole("read","download");
        var condition=new cn.sparrowmini.datapolicy.core.Condition.Equal("id",one.id());
        publish("read",List.of(rule(role,"read",condition,cn.sparrowmini.datapolicy.core.DataRule.Target.ROW,Set.of()),rule(role,"read",condition,cn.sparrowmini.datapolicy.core.DataRule.Target.FIELD_READ,Set.of("id"))));
        publish("download",List.of(rule(role,"download",condition,cn.sparrowmini.datapolicy.core.DataRule.Target.ROW,Set.of())));
        var session=login(reader);
        http.perform(get("/api/files?size=1").session(session)).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1)).andExpect(jsonPath("$.content[0].id").value(one.id())).andExpect(jsonPath("$.content[0].name").doesNotExist());
        http.perform(get("/api/files/"+one.id()+"/content").session(session)).andExpect(status().isOk()).andExpect(content().string("content"));
        http.perform(get("/api/files/"+two.id()+"/content").session(session)).andExpect(status().isForbidden());
        http.perform(post("/api/admin/policies/preview").session(login(admin)).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content("{\"subject\":\"default:"+reader.id()+"\",\"scope\":\"read\",\"recordId\":\""+one.id()+"\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.recordAllowed").value(true)).andExpect(jsonPath("$.fields.id.readable").value(true)).andExpect(jsonPath("$.fields.name.readable").value(false));
        access.initialize(()->fileAdministration.requestDelete(one.id(),one.version(),null));
        http.perform(get("/api/files?size=1").session(session)).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(0));
    }
    @Test void fileRenameCannotEscapeItsOriginalRange()throws Exception{
        var file=sampleFile("fixed.txt");String role=businessRole("update");var condition=new cn.sparrowmini.datapolicy.core.Condition.Equal("name","fixed.txt");
        publish("update",List.of(rule(role,"update",condition,cn.sparrowmini.datapolicy.core.DataRule.Target.ROW,Set.of()),rule(role,"update",condition,cn.sparrowmini.datapolicy.core.DataRule.Target.FIELD_WRITE,Set.of("name"))));
        http.perform(put("/api/files/"+file.id()).session(login(reader)).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"version\":"+file.version()+",\"name\":\"outside.txt\"}")).andExpect(status().isForbidden());
        access.initialize(()->assertEquals("fixed.txt",fileAdministration.get(file.id(),null).name()));
    }
    @Test void businessParentRestrictionFurtherNarrowsFilePolicy()throws Exception{
        var file=sampleFile("attached.txt");String role=businessRole("read","download");var condition=new cn.sparrowmini.datapolicy.core.Condition.Equal("id",file.id());
        publish("read",List.of(rule(role,"read",condition,cn.sparrowmini.datapolicy.core.DataRule.Target.ROW,Set.of()),rule(role,"read",condition,cn.sparrowmini.datapolicy.core.DataRule.Target.FIELD_READ,Set.of("id"))));
        publish("download",List.of(rule(role,"download",condition,cn.sparrowmini.datapolicy.core.DataRule.Target.ROW,Set.of())));
        new org.springframework.transaction.support.TransactionTemplate(transactions).executeWithoutResult(status->new cn.sparrowmini.file.FileReferenceService(em,"default",(op,id,r,record)->{}).attach(file.id(),"invoice","private","receipt"));
        var session=login(reader);http.perform(get("/api/files").session(session)).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(0));
        http.perform(get("/api/files/"+file.id()+"/content").session(session)).andExpect(status().isForbidden());
    }

    @Autowired cn.sparrowmini.system.presentation.UiPageService uiPages;
    @Test void persistedUiRulesUseCurrentRolesAndNeverGrantBackendPermission()throws Exception{
        String page="page-"+UUID.randomUUID();
        var element=new cn.sparrowmini.system.presentation.UiCapabilityService.Element("save",null,cn.sparrowmini.system.presentation.UiCapabilityService.Kind.BUTTON,false,new PermissionRequest("admin.users","read"),new PermissionRequest("admin.users","manage"),null,cn.sparrowmini.system.presentation.UiCapabilityService.Mode.RESTRICTED,false);
        var policy=new cn.sparrowmini.system.presentation.UiCapabilityService.Policy("save",new Subject(Subject.Kind.ROLE,adminRole),cn.sparrowmini.system.presentation.UiCapabilityService.Operation.VIEW,Effect.ALLOW);
        var interact=new cn.sparrowmini.system.presentation.UiCapabilityService.Policy("save",new Subject(Subject.Kind.ROLE,adminRole),cn.sparrowmini.system.presentation.UiCapabilityService.Operation.INTERACT,Effect.ALLOW);
        access.initialize(()->uiPages.save(page,null,new cn.sparrowmini.system.presentation.UiPageService.Definition(List.of(element),List.of(policy,interact))));
        String body="{\"page\":\""+page+"\",\"elements\":[\"save\"]}";
        http.perform(post("/api/ui/capabilities").session(login(admin)).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isOk()).andExpect(jsonPath("$.save.enabled").value(true));
        http.perform(post("/api/ui/capabilities").session(login(reader)).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isOk()).andExpect(jsonPath("$.save.visible").value(false));
        access.initialize(()->assertThrows(jakarta.persistence.OptimisticLockException.class,()->uiPages.save(page,null,new cn.sparrowmini.system.presentation.UiPageService.Definition(List.of(element),List.of()))));
    }
    @Test void unknownUiPagesFailClosed()throws Exception{
        http.perform(post("/api/ui/capabilities").session(login(reader)).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content("{\"page\":\"missing\",\"elements\":[\"save\"]}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.save.visible").value(false));
    }

    @Test void clientConfigurationReflectsVersionedAdminChanges()throws Exception{
        var session=login(admin);
        http.perform(put("/api/admin/configurations/ui.brand").session(session).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content("{\"version\":null,\"value\":\"Project administration\"}"))
            .andExpect(status().isOk());
        http.perform(get("/api/configuration").session(login(reader))).andExpect(status().isOk()).andExpect(jsonPath("$['ui.brand']").value("Project administration"));
        http.perform(put("/api/admin/configurations/ui.brand").session(login(reader)).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content("{\"version\":0,\"value\":\"forged\"}")).andExpect(status().isForbidden());
    }

    @Test void hierarchyManagementRequiresPermissionAndPreservesVersion()throws Exception{
        http.perform(post("/api/admin/trees").session(login(reader)).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Forbidden\",\"mode\":\"TREE\"}")).andExpect(status().isForbidden());
        var session=login(admin);var created=http.perform(post("/api/admin/trees").session(session).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Categories\",\"mode\":\"DAG\"}")).andExpect(status().isOk()).andReturn();
        String id=new com.fasterxml.jackson.databind.ObjectMapper().readTree(created.getResponse().getContentAsString()).get("id").asText();
        String body="{\"revision\":0,\"order\":\"0.125\"}";
        http.perform(post("/api/admin/trees/"+id+"/nodes").session(session).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isOk()).andExpect(jsonPath("$.revision").value(1));
        http.perform(post("/api/admin/trees/"+id+"/nodes").session(session).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isConflict());
    }
}
