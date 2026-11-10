package cn.sparrowmini.rbac.persistence.jpa.catalog;
import cn.sparrowmini.security.api.*;
import jakarta.persistence.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
/** A live, namespace-isolated directory. Management authorization is supplied by the host. */
@Transactional(readOnly=true)
public class JpaPermissionCatalog implements PermissionCatalog {
 public interface Guard { void require(String operation); }
 public record Resource(String code,String name,String description,boolean enabled,long version){}
 public record Scope(String resource,String code,String name,String description,boolean enabled,long version){}
 private final EntityManager em;private final String namespace;private final Guard guard;
 public JpaPermissionCatalog(EntityManager em,String namespace,Guard guard){this.em=Objects.requireNonNull(em);this.namespace=Objects.requireNonNull(namespace);this.guard=Objects.requireNonNull(guard);}
 public Set<PermissionRequest> permissions(){return Set.copyOf(em.createQuery("select s from PermissionScopeEntity s join fetch s.resource r where r.namespace=:ns and r.enabled=true and s.enabled=true",PermissionScopeEntity.class).setParameter("ns",namespace).getResultList().stream().map(s->new PermissionRequest(s.resource.code,s.code)).toList());}
 public boolean exists(PermissionRequest p){return scope(p).isPresent();}
 public boolean isEnabled(PermissionRequest p){return scope(p).map(s->s.enabled&&s.resource.enabled).orElse(false);}
 public List<Resource> resources(){guard.require("read");return em.createQuery("select r from PermissionResourceEntity r where r.namespace=:ns order by r.code",PermissionResourceEntity.class).setParameter("ns",namespace).getResultList().stream().map(this::view).toList();}
 public List<Scope> scopes(){guard.require("read");return em.createQuery("select s from PermissionScopeEntity s join fetch s.resource r where r.namespace=:ns order by r.code,s.code",PermissionScopeEntity.class).setParameter("ns",namespace).getResultList().stream().map(s->new Scope(s.resource.code,s.code,s.name,s.description,s.enabled,s.version)).toList();}
 @Transactional public Resource saveResource(Resource input){guard.require("manage");code(input.code());name(input.name());var found=resource(input.code());var r=found.orElseGet(PermissionResourceEntity::new);if(found.isPresent()&&r.version!=input.version())throw new OptimisticLockException("Resource changed");if(found.isEmpty()){if(input.version()!=-1)throw new IllegalArgumentException("New resource requires version -1");r.id=UUID.randomUUID().toString();r.namespace=namespace;r.code=input.code();}r.name=input.name();r.description=input.description();r.enabled=input.enabled();if(found.isEmpty())em.persist(r);em.flush();return view(r);}
 @Transactional public Scope saveScope(Scope input){guard.require("manage");code(input.code());name(input.name());var parent=resource(input.resource()).orElseThrow(()->new IllegalArgumentException("Unknown resource"));var found=scope(new PermissionRequest(input.resource(),input.code()));var s=found.orElseGet(PermissionScopeEntity::new);if(found.isPresent()&&s.version!=input.version())throw new OptimisticLockException("Scope changed");if(found.isEmpty()){if(input.version()!=-1)throw new IllegalArgumentException("New scope requires version -1");s.id=UUID.randomUUID().toString();s.resource=parent;s.code=input.code();}s.name=input.name();s.description=input.description();s.enabled=input.enabled();if(found.isEmpty())em.persist(s);em.flush();return new Scope(parent.code,s.code,s.name,s.description,s.enabled,s.version);}
 private Optional<PermissionResourceEntity> resource(String code){return em.createQuery("select r from PermissionResourceEntity r where r.namespace=:ns and r.code=:code",PermissionResourceEntity.class).setParameter("ns",namespace).setParameter("code",code).getResultStream().findFirst();}
 private Optional<PermissionScopeEntity> scope(PermissionRequest p){return em.createQuery("select s from PermissionScopeEntity s join fetch s.resource r where r.namespace=:ns and r.code=:resource and s.code=:scope",PermissionScopeEntity.class).setParameter("ns",namespace).setParameter("resource",p.resource()).setParameter("scope",p.scope()).getResultStream().findFirst();}
 private Resource view(PermissionResourceEntity r){return new Resource(r.code,r.name,r.description,r.enabled,r.version);}
 private static void code(String value){if(value==null||value.length()>100||!value.matches("[a-zA-Z0-9][a-zA-Z0-9._-]*"))throw new IllegalArgumentException("Invalid permission code");}
 private static void name(String value){if(value==null||value.isBlank()||value.length()>255)throw new IllegalArgumentException("Name required (max 255)");}
}
