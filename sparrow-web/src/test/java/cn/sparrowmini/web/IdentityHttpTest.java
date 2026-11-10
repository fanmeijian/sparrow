package cn.sparrowmini.web;

import cn.sparrowmini.identity.api.*;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;
import java.util.List;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class IdentityHttpTest {
    static class Directory implements UserDirectoryProvider {
        public String code(){return "external";}
        public UserProfile get(String id){throw new SecurityException("secret internal detail");}
        public List<UserProfile> list(String prefix,int offset,int limit){
            return List.of(new UserProfile("external","test","u1","alice","Alice",true,1));
        }
    }
    MockMvc http(UserDirectoryProvider provider){return MockMvcBuilders.standaloneSetup(new IdentityController(provider)).setControllerAdvice(new ApiErrors()).build();}
    @Test void externalDirectoryCanListWithoutLocalDatabase()throws Exception{
        http(new Directory()).perform(get("/api/admin/users")).andExpect(status().isOk())
            .andExpect(jsonPath("$[0].provider").value("external")).andExpect(jsonPath("$[0].password").doesNotExist());
    }
    @Test void readOnlyDirectoryRejectsMutation()throws Exception{
        http(new Directory()).perform(post("/api/admin/users").contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"alice\",\"displayName\":\"Alice\",\"password\":\"secret\"}"))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("OPERATION_UNAVAILABLE"));
    }
    @Test void forbiddenResponseDoesNotExposeInternalMessage()throws Exception{
        http(new Directory()).perform(get("/api/admin/users/u1")).andExpect(status().isForbidden())
            .andExpect(content().json("{\"code\":\"ACCESS_DENIED\"}"));
    }
    @Test void staleVersionIsConflict()throws Exception{
        http(new Directory(){public UserProfile get(String id){throw new OptimisticLockException("sql detail");}})
            .perform(get("/api/admin/users/u1")).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("STALE_VERSION"));
    }
    @Test void malformedBodyIsBadRequest()throws Exception{
        http(new Directory()).perform(post("/api/admin/users").contentType(MediaType.APPLICATION_JSON).content("{"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }
}
