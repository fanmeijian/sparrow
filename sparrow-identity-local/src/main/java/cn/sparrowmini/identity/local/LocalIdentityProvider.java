package cn.sparrowmini.identity.local;

import cn.sparrowmini.identity.api.*;
import jakarta.persistence.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import java.text.Normalizer;
import java.util.*;

/** JPA persistence with Spring-managed transactions in production. Authentication never exposes profile writes. */
public class LocalIdentityProvider implements UserLifecycleProvider, UserDetailsService {
    private final EntityManager em;
    private final PasswordEncoder encoder;
    private final IdentityManagementGuard guard;
    private final String namespace;
    public LocalIdentityProvider(EntityManager em,PasswordEncoder encoder,IdentityManagementGuard guard,String namespace) {
        this.em=Objects.requireNonNull(em);this.encoder=Objects.requireNonNull(encoder);this.guard=Objects.requireNonNull(guard);
        if(namespace==null || !namespace.matches("[a-zA-Z0-9_-]{1,100}"))throw new IllegalArgumentException("Invalid namespace");
        this.namespace=namespace;
    }
    public String code(){return "local";}
    @Transactional(readOnly=true)
    public UserProfile get(String id){guard.require(namespace,"read",id);return profile(account(id));}
    @Transactional(readOnly=true)
    public List<UserProfile> list(String prefix,int offset,int limit){
        guard.require(namespace,"read",null);
        if(offset<0 || limit<1 || limit>500)throw new IllegalArgumentException("Invalid pagination");
        var cb=em.getCriteriaBuilder();var q=cb.createQuery(LocalAccount.class);var root=q.from(LocalAccount.class);
        String text=prefix==null?"":normalize(prefix);
        String escaped=text.replace("\\","\\\\").replace("%","\\%").replace("_","\\_");
        q.select(root).where(cb.equal(root.get("namespace"),namespace),cb.like(root.get("username"),escaped+"%",'\\')).orderBy(cb.asc(root.get("username")));
        return em.createQuery(q).setFirstResult(offset).setMaxResults(limit).getResultList().stream().map(this::profile).toList();
    }
    @Transactional
    public UserProfile create(String username,String displayName,String password){
        guard.require(namespace,"create",null);requireTransaction();validatePassword(password);validateDisplay(displayName);
        String login=normalize(username);if(login.isBlank() || login.length()>150)throw new IllegalArgumentException("Invalid username");
        if(findLogin(login).isPresent())throw new IllegalArgumentException("Username already exists");
        var account=new LocalAccount();account.id=UUID.randomUUID().toString();account.namespace=namespace;account.username=login;
        account.displayName=displayName;account.passwordHash=encoder.encode(password);account.enabled=true;
        em.persist(account);em.flush();return profile(account);
    }
    @Transactional
    public UserProfile update(String id,long expectedVersion,String displayName){
        guard.require(namespace,"update",id);requireTransaction();validateDisplay(displayName);
        var account=locked(id,expectedVersion);account.displayName=displayName;em.flush();return profile(account);
    }
    @Transactional
    public UserProfile setEnabled(String id,long expectedVersion,boolean enabled){
        guard.require(namespace,"enable",id);requireTransaction();var account=locked(id,expectedVersion);
        if(account.enabled!=enabled){account.enabled=enabled;account.credentialVersion++;}
        em.flush();return profile(account);
    }
    @Transactional
    public void resetPassword(String id,long expectedVersion,String password){
        guard.require(namespace,"reset-credential",id);requireTransaction();validatePassword(password);
        var account=locked(id,expectedVersion);account.passwordHash=encoder.encode(password);account.credentialVersion++;account.failedAttempts=0;account.lockedUntil=0;em.flush();
    }
    @Transactional(readOnly=true)
    public UserDetails loadUserByUsername(String username){
        var account=findLogin(normalize(username)).orElseThrow(()->new UsernameNotFoundException("Invalid credentials"));
        return new LocalPrincipal(account.id,namespace,account.username,account.passwordHash,account.enabled,account.credentialVersion);
    }
    /** Called on every local authenticated request; scalar query avoids stale entity state. */
    @Transactional(readOnly=true)
    public boolean isCurrent(LocalPrincipal principal){
        if(!namespace.equals(principal.namespace()))return false;
        var cb=em.getCriteriaBuilder();var q=cb.createTupleQuery();var root=q.from(LocalAccount.class);
        q.multiselect(root.get("enabled"),root.get("credentialVersion"))
            .where(cb.equal(root.get("id"),principal.subjectId()),cb.equal(root.get("namespace"),namespace));
        var rows=em.createQuery(q).getResultList();
        return rows.size()==1 && Boolean.TRUE.equals(rows.get(0).get(0)) && Objects.equals(rows.get(0).get(1),principal.credentialVersion());
    }
    /** Authentication-only operations; counts are shared by every node using this database. */
    @Transactional(readOnly=true)
    public boolean loginLocked(String username,long now){return findLogin(normalize(username)).map(a->a.lockedUntil>now).orElse(false);}
    @Transactional
    public void loginFailed(String username,int maximum,long lockMillis,long now){
        if(maximum<1 || lockMillis<1)throw new IllegalArgumentException("Invalid login limits");requireTransaction();
        var found=findLogin(normalize(username));if(found.isEmpty())return;
        var account=found.get();em.refresh(account,LockModeType.PESSIMISTIC_WRITE);
        if(account.lockedUntil>now)return;
        if(account.lockedUntil!=0){account.failedAttempts=0;account.lockedUntil=0;}
        account.failedAttempts++;
        if(account.failedAttempts>=maximum)account.lockedUntil=Math.addExact(now,lockMillis);
        em.flush();
    }
    @Transactional
    public boolean loginSucceeded(LocalPrincipal principal,long now){
        requireTransaction();if(!namespace.equals(principal.namespace()))return false;
        var account=em.find(LocalAccount.class,principal.subjectId(),LockModeType.PESSIMISTIC_WRITE);if(account==null)return false;
        em.refresh(account);
        if(!account.enabled || account.credentialVersion!=principal.credentialVersion() || account.lockedUntil>now)return false;
        if(account.failedAttempts!=0 || account.lockedUntil!=0){account.failedAttempts=0;account.lockedUntil=0;em.flush();}return true;
    }
    private Optional<LocalAccount> findLogin(String login){
        var cb=em.getCriteriaBuilder();var q=cb.createQuery(LocalAccount.class);var root=q.from(LocalAccount.class);
        q.select(root).where(cb.equal(root.get("namespace"),namespace),cb.equal(root.get("username"),login));
        return em.createQuery(q).getResultStream().findFirst();
    }
    private LocalAccount account(String id){
        var account=em.find(LocalAccount.class,Objects.requireNonNull(id));
        if(account==null || !namespace.equals(account.namespace))throw new IllegalArgumentException("User not found");
        return account;
    }
    private LocalAccount locked(String id,long version){
        var account=account(id);em.refresh(account,LockModeType.PESSIMISTIC_WRITE);
        if(account.version!=version)throw new OptimisticLockException("Stale account version");
        return account;
    }
    private void requireTransaction(){if(!em.isJoinedToTransaction())throw new IllegalStateException("Active transaction required");}
    private static String normalize(String value){return Normalizer.normalize(Objects.requireNonNull(value).strip(),Normalizer.Form.NFKC).toLowerCase(Locale.ROOT);}
    private static void validateDisplay(String value){if(value==null || value.isBlank() || value.length()>200)throw new IllegalArgumentException("Invalid display name");}
    private static void validatePassword(String value){
        if(value==null || value.length()<12 || value.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)throw new IllegalArgumentException("Password must contain at least 12 characters and at most 72 UTF-8 bytes");
    }
    private UserProfile profile(LocalAccount a){return new UserProfile(code(),namespace,a.id,a.username,a.displayName,a.enabled,a.version);}
}
