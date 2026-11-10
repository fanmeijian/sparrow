package cn.sparrowmini.identity.local;

import cn.sparrowmini.identity.api.*;
import jakarta.persistence.*;
import org.junit.jupiter.api.*;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import java.util.*;
import java.util.function.Supplier;
import static org.junit.jupiter.api.Assertions.*;

class LocalIdentityTest {
    static EntityManagerFactory emf;
    EntityManager em;
    LocalIdentityProvider users;
    final String password="Valid-passphrase-123";
    @BeforeAll static void open(){emf=Persistence.createEntityManagerFactory("identity-test");}
    @AfterAll static void close(){emf.close();}
    @BeforeEach void setup(){
        em=emf.createEntityManager();users=provider("tenant",(n,s,id)->{});
        tx(()->{var cb=em.getCriteriaBuilder();var q=cb.createCriteriaDelete(LocalAccount.class);q.from(LocalAccount.class);em.createQuery(q).executeUpdate();return null;});
    }
    @AfterEach void cleanup(){if(em.getTransaction().isActive())em.getTransaction().rollback();em.close();}
    LocalIdentityProvider provider(String namespace,IdentityManagementGuard guard){return new LocalIdentityProvider(em,PasswordEncoderFactories.createDelegatingPasswordEncoder(),guard,namespace);}
    <T> T tx(Supplier<T> action){
        em.getTransaction().begin();
        try{T result=action.get();em.getTransaction().commit();em.clear();return result;}
        catch(RuntimeException e){if(em.getTransaction().isActive())em.getTransaction().rollback();em.clear();throw e;}
    }
    LocalPrincipal login(String username,String password){
        var dao=new DaoAuthenticationProvider(users);dao.setPasswordEncoder(PasswordEncoderFactories.createDelegatingPasswordEncoder());
        return (LocalPrincipal)new ProviderManager(dao).authenticate(UsernamePasswordAuthenticationToken.unauthenticated(username,password)).getPrincipal();
    }
    @Test void authenticateNormalizedUserAndKeepHashOutOfProfile(){
        var user=tx(()->users.create(" Alice ","Alice",password));
        assertEquals("alice",user.username());
        var account=em.find(LocalAccount.class,user.id());assertTrue(account.passwordHash.startsWith("{bcrypt}"));assertNotEquals(password,account.passwordHash);
        var principal=login("ALICE",password);assertEquals(user.id(),principal.subjectId());assertNull(principal.getPassword());assertTrue(users.isCurrent(principal));
        assertFalse(user.toString().contains("hash"));
    }
    @Test void wrongAndMissingCredentialsFail(){
        tx(()->users.create("alice","Alice",password));
        assertThrows(BadCredentialsException.class,()->login("alice","incorrect"));
        assertThrows(BadCredentialsException.class,()->login("absent","incorrect"));
    }
    @Test void passwordResetInvalidatesOldSession(){
        var user=tx(()->users.create("alice","Alice",password));var old=login("alice",password);
        tx(()->{users.resetPassword(user.id(),user.version(),"New-passphrase-456");return null;});
        assertFalse(users.isCurrent(old));assertThrows(BadCredentialsException.class,()->login("alice",password));
        assertTrue(users.isCurrent(login("alice","New-passphrase-456")));
    }
    @Test void disableInvalidatesExistingSessionAndLogin(){
        var user=tx(()->users.create("alice","Alice",password));var session=login("alice",password);
        var disabled=tx(()->users.setEnabled(user.id(),user.version(),false));
        assertFalse(users.isCurrent(session));assertThrows(DisabledException.class,()->login("alice",password));
        tx(()->users.setEnabled(user.id(),disabled.version(),true));assertFalse(users.isCurrent(session));assertTrue(users.isCurrent(login("alice",password)));
    }
    @Test void namespaceIsolationAndUniqueLogin(){
        var user=tx(()->users.create("alice","Alice",password));
        assertThrows(IllegalArgumentException.class,()->tx(()->users.create("ALICE","Duplicate",password)));
        var other=provider("other",(n,s,id)->{});assertThrows(IllegalArgumentException.class,()->other.get(user.id()));
        assertFalse(other.isCurrent(login("alice",password)));
        tx(()->other.create("alice","Other Alice",password));assertEquals(1,users.list("",0,10).size());
    }
    @Test void unauthorizedAndStaleWritesLeaveAccountUnchanged(){
        var user=tx(()->users.create("alice","Alice",password));
        var denied=provider("tenant",(n,s,id)->{throw new SecurityException("Not delegated");});
        assertThrows(SecurityException.class,()->tx(()->denied.setEnabled(user.id(),user.version(),false)));
        tx(()->users.update(user.id(),user.version(),"Renamed"));
        assertThrows(OptimisticLockException.class,()->tx(()->users.update(user.id(),user.version(),"Lost update")));
        assertEquals("Renamed",users.get(user.id()).displayName());assertTrue(users.get(user.id()).enabled());
    }
    @Test void missingOrDuplicateProviderDoesNotFallBack(){
        assertSame(users,new IdentityProviderRegistry(List.of(users)).select("local"));
        assertThrows(IllegalArgumentException.class,()->new IdentityProviderRegistry(List.of(users)).select("keycloak"));
        assertThrows(IllegalArgumentException.class,()->new IdentityProviderRegistry(List.of(users,users)));
    }
    @Test void invalidPasswordsAndUnboundedQueriesAreRejected(){
        assertThrows(IllegalArgumentException.class,()->tx(()->users.create("alice","Alice","short")));
        assertThrows(IllegalArgumentException.class,()->tx(()->users.create("alice","Alice","汉".repeat(30))));
        assertThrows(IllegalArgumentException.class,()->users.list("",0,10000));
        assertEquals(0,users.list("",0,10).size());
    }

    @Test void failedLoginLimitExpiresAndIsSharedThroughDatabase(){
        tx(()->users.create("alice","Alice",password));
        tx(()->{users.loginFailed("alice",2,1000,100);return null;});assertFalse(users.loginLocked("ALICE",100));
        tx(()->{users.loginFailed("alice",2,1000,100);return null;});assertTrue(users.loginLocked("alice",1099));
        assertFalse(users.loginLocked("alice",1100));
        var other=provider("tenant",(n,s,id)->{});assertTrue(other.loginLocked("alice",100));
        var principal=login("alice",password);assertFalse(tx(()->users.loginSucceeded(principal,100)));
        assertTrue(tx(()->users.loginSucceeded(principal,1100)));assertFalse(users.loginLocked("alice",1100));
    }
    @Test void staleCredentialCannotCompleteConcurrentAuthentication(){
        var user=tx(()->users.create("alice","Alice",password));var principal=login("alice",password);
        tx(()->{users.resetPassword(user.id(),user.version(),"Changed-passphrase-456");return null;});
        assertFalse(tx(()->users.loginSucceeded(principal,100)));
    }
}
