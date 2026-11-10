package cn.sparrowmini.rbac.persistence.jpa.grant;

import cn.sparrowmini.rbac.core.*;
import cn.sparrowmini.security.api.*;
import jakarta.persistence.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

/** Stores resource/scope grants for local or external role IDs, with no local-role entity dependency. */
public class JpaRoleGrantStore implements RolePermissionManager,RoleGrantSource {
    private final EntityManager em;private final String namespace;private final PermissionCatalog registered;private final RoleReferenceValidator roles;private final GrantManagementGuard guard;
        public JpaRoleGrantStore(EntityManager em,String namespace,PermissionCatalog registered,RoleReferenceValidator roles,GrantManagementGuard guard){this.em=Objects.requireNonNull(em);this.namespace=Objects.requireNonNull(namespace);this.registered=Objects.requireNonNull(registered);this.roles=Objects.requireNonNull(roles);this.guard=Objects.requireNonNull(guard);}
    @Transactional public void grant(String role,PermissionRequest permission,Effect effect){
        transaction();Objects.requireNonNull(effect);known(permission);roles.require(role);var existing=lookup(role,permission,true);var before=existing.map(g->g.effect).orElse(null);guard.require(namespace,"grant",role,permission,before,effect);
        RolePermissionEntity entity;
        if(existing.isPresent())entity=existing.get();else{entity=new RolePermissionEntity();entity.id=UUID.randomUUID().toString();entity.namespace=namespace;entity.roleId=role;entity.resource=permission.resource();entity.scope=permission.scope();entity.effect=effect;em.persist(entity);}
        entity.effect=effect;em.flush();
    }
    @Transactional public void revokePermission(String role,PermissionRequest permission){
        transaction();if(!registered.exists(permission))throw new IllegalArgumentException("Unknown resource/scope");roles.require(role);var existing=lookup(role,permission,true);guard.require(namespace,"revoke-permission",role,permission,existing.map(g->g.effect).orElse(null),null);existing.ifPresent(em::remove);em.flush();
    }
    @Transactional(readOnly=true) public List<RoleGrant> grants(String role){
        // Stored grants remain readable even when the external role directory is unavailable.
        guard.require(namespace,"read",role,null,null,null);var cb=em.getCriteriaBuilder();var q=cb.createQuery(RolePermissionEntity.class);var r=q.from(RolePermissionEntity.class);
        q.select(r).where(cb.equal(r.get("namespace"),namespace),cb.equal(r.get("roleId"),role)).orderBy(cb.asc(r.get("resource")),cb.asc(r.get("scope")));return em.createQuery(q).getResultList().stream().map(this::view).toList();
    }
    @Transactional(readOnly=true) public List<RoleGrant> find(Set<String> roles,PermissionRequest permission){
        if(roles.isEmpty()||!registered.isEnabled(permission))return List.of();var cb=em.getCriteriaBuilder();var q=cb.createQuery(RolePermissionEntity.class);var r=q.from(RolePermissionEntity.class);
        q.select(r).where(cb.equal(r.get("namespace"),namespace),r.get("roleId").in(roles),cb.equal(r.get("resource"),permission.resource()),cb.equal(r.get("scope"),permission.scope()));return em.createQuery(q).getResultList().stream().map(this::view).toList();
    }
    private Optional<RolePermissionEntity> lookup(String role,PermissionRequest permission,boolean lock){var cb=em.getCriteriaBuilder();var q=cb.createQuery(RolePermissionEntity.class);var r=q.from(RolePermissionEntity.class);q.select(r).where(cb.equal(r.get("namespace"),namespace),cb.equal(r.get("roleId"),role),cb.equal(r.get("resource"),permission.resource()),cb.equal(r.get("scope"),permission.scope()));var query=em.createQuery(q);if(lock)query.setLockMode(LockModeType.PESSIMISTIC_WRITE);var found=query.getResultStream().findFirst();if(lock)found.ifPresent(em::refresh);return found;}
    private RoleGrant view(RolePermissionEntity entity){return new RoleGrant(entity.roleId,new PermissionRequest(entity.resource,entity.scope),entity.effect);}
    private void known(PermissionRequest permission){if(!registered.isEnabled(permission))throw new IllegalArgumentException("Unknown resource/scope");}
    private void transaction(){if(!em.isJoinedToTransaction())throw new IllegalStateException("Active transaction required");}
}
