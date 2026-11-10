package cn.sparrowmini.rbac.persistence.jpa;

import cn.sparrowmini.rbac.core.*;
import cn.sparrowmini.rbac.persistence.jpa.grant.JpaRoleGrantStore;
import cn.sparrowmini.security.api.*;
import jakarta.persistence.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

public class JpaRoleProvider implements RoleDirectoryProvider,RoleAssignmentProvider,RolePermissionManager,FunctionPermissionEvaluator {
    @Override public boolean supportsRoleEvidence(){return true;}
    private final EntityManager em;
    private final String namespace;
    private final RbacManagementGuard guard;
    private final PermissionCatalog registered;
    private final JpaRoleGrantStore permissionStore;
        public JpaRoleProvider(EntityManager em,String namespace,RbacManagementGuard guard,PermissionCatalog registered){
        this.em=Objects.requireNonNull(em);this.namespace=Objects.requireNonNull(namespace);this.guard=Objects.requireNonNull(guard);this.registered=Objects.requireNonNull(registered);
        this.permissionStore=new JpaRoleGrantStore(em,namespace,registered,this::requireRole,(n,op,id,permission,before,after)->guard.require(n,op,id));
        if(namespace.isBlank() || namespace.length()>100)throw new IllegalArgumentException("Invalid namespace");
    }
    public String code(){return "local";}
    @Transactional(readOnly=true)
    public List<RoleProfile> list(int offset,int limit){
        guard.require(namespace,"read",null);if(offset<0 || limit<1 || limit>500)throw new IllegalArgumentException("Invalid pagination");
        var cb=em.getCriteriaBuilder();var q=cb.createQuery(RoleEntity.class);var root=q.from(RoleEntity.class);
        q.select(root).where(cb.equal(root.get("namespace"),namespace)).orderBy(cb.asc(root.get("code")));
        return em.createQuery(q).setFirstResult(offset).setMaxResults(limit).getResultList().stream().map(this::profile).toList();
    }
    @Transactional
    public RoleProfile create(String code,String name){
        guard.require(namespace,"create",null);transaction();
        if(code==null || !code.matches("[a-zA-Z0-9_.-]{1,100}") || name==null || name.isBlank() || name.length()>200)throw new IllegalArgumentException("Invalid role");
        var role=new RoleEntity();role.id=UUID.randomUUID().toString();role.namespace=namespace;role.code=code;role.name=name;role.enabled=true;
        em.persist(role);em.flush();return profile(role);
    }
    @Transactional
    public RoleProfile setEnabled(String id,long version,boolean enabled){
        guard.require(namespace,"update",id);transaction();var role=lock(id);
        if(role.version!=version)throw new OptimisticLockException("Stale role version");
        role.enabled=enabled;em.flush();return profile(role);
    }
    @Transactional
    public void assign(String roleId,Subject subject){
        guard.require(namespace,"assign-role",roleId);transaction();validSubject(subject);var role=lock(roleId);
        if(!assignments(roleId,subject).isEmpty())return;
        var a=new RoleAssignmentEntity();a.id=UUID.randomUUID().toString();a.role=role;a.kind=subject.kind();a.subjectId=subject.id();em.persist(a);em.flush();
    }
    @Transactional
    public void revoke(String roleId,Subject subject){
        guard.require(namespace,"revoke-role",roleId);transaction();validSubject(subject);lock(roleId);
        assignments(roleId,subject).forEach(em::remove);em.flush();
    }
    @Transactional
    public void grant(String roleId,PermissionRequest request,Effect effect){guard.require(namespace,"grant",roleId);transaction();lock(roleId);permissionStore.grant(roleId,request,effect);}
    @Transactional
    public void revokePermission(String roleId,PermissionRequest request){guard.require(namespace,"revoke-permission",roleId);transaction();lock(roleId);permissionStore.revokePermission(roleId,request);}
    @Transactional(readOnly=true)
    public Set<String> rolesFor(Subject subject){
        validSubject(subject);var cb=em.getCriteriaBuilder();var q=cb.createQuery(String.class);var root=q.from(RoleAssignmentEntity.class);
        q.select(root.get("role").get("id")).distinct(true).where(cb.equal(root.get("kind"),subject.kind()),cb.equal(root.get("subjectId"),subject.id()),cb.equal(root.get("role").get("namespace"),namespace),cb.isTrue(root.get("role").get("enabled")));
        return Set.copyOf(em.createQuery(q).getResultList());
    }
    @Transactional(readOnly=true)
    public FunctionDecision evaluate(ActorContext actor,PermissionRequest request){
        Set<String> roles=new HashSet<>(rolesFor(new Subject(Subject.Kind.USER,actor.subject())));
        for(String group:actor.groups())roles.addAll(rolesFor(new Subject(Subject.Kind.GROUP,group)));
        var current=new ActorContext(actor.subject(),roles,actor.groups());
        if(!registered.isEnabled(request) || roles.isEmpty())return new FunctionDecision(current,request,false,Set.of());
        return new RolePermissionEvaluator(registered,permissionStore.find(roles,request)).evaluate(current,request);
    }
    @Transactional(readOnly=true)
    public List<RoleGrant> grants(String roleId){return permissionStore.grants(roleId);}
    @Transactional(readOnly=true)
    public Set<Subject> members(String roleId){
        guard.require(namespace,"read",roleId);requireRole(roleId);
        var cb=em.getCriteriaBuilder();var q=cb.createQuery(RoleAssignmentEntity.class);var r=q.from(RoleAssignmentEntity.class);
        q.select(r).where(cb.equal(r.get("role").get("id"),roleId));
        Set<Subject> result=new HashSet<>();em.createQuery(q).getResultList().forEach(a->result.add(new Subject(a.kind,a.subjectId)));return Set.copyOf(result);
    }
    private void requireRole(String id){var role=em.find(RoleEntity.class,id);if(role==null||!namespace.equals(role.namespace))throw new IllegalArgumentException("Role not found");}
    private List<RoleAssignmentEntity> assignments(String roleId,Subject subject){
        var cb=em.getCriteriaBuilder();var q=cb.createQuery(RoleAssignmentEntity.class);var root=q.from(RoleAssignmentEntity.class);
        q.select(root).where(cb.equal(root.get("role").get("id"),roleId),cb.equal(root.get("kind"),subject.kind()),cb.equal(root.get("subjectId"),subject.id()));
        return em.createQuery(q).getResultList();
    }
    private RoleEntity lock(String id){
        var role=em.find(RoleEntity.class,id,LockModeType.PESSIMISTIC_WRITE);
        if(role==null || !namespace.equals(role.namespace))throw new IllegalArgumentException("Role not found");
        em.refresh(role,LockModeType.PESSIMISTIC_WRITE);return role;
    }
    private static void validSubject(Subject subject){if(subject.kind()==Subject.Kind.ROLE)throw new IllegalArgumentException("Role nesting is not a membership assignment");}
    private void transaction(){if(!em.isJoinedToTransaction())throw new IllegalStateException("Active transaction required");}
    private RoleProfile profile(RoleEntity r){return new RoleProfile(r.id,r.code,r.name,r.enabled,r.version);}
}
