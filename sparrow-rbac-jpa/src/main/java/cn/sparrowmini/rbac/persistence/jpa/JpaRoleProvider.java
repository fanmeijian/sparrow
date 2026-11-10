package cn.sparrowmini.rbac.persistence.jpa;

import cn.sparrowmini.rbac.core.*;
import cn.sparrowmini.security.api.*;
import jakarta.persistence.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

public class JpaRoleProvider implements RoleDirectoryProvider,RoleAssignmentProvider,RolePermissionManager,FunctionPermissionEvaluator {
    private final EntityManager em;
    private final String namespace;
    private final RbacManagementGuard guard;
    private final Set<PermissionRequest> registered;
    public JpaRoleProvider(EntityManager em,String namespace,RbacManagementGuard guard,Set<PermissionRequest> registered){
        this.em=Objects.requireNonNull(em);this.namespace=Objects.requireNonNull(namespace);this.guard=Objects.requireNonNull(guard);this.registered=Set.copyOf(registered);
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
    public void grant(String roleId,PermissionRequest request,Effect effect){
        guard.require(namespace,"grant",roleId);transaction();Objects.requireNonNull(effect);
        if(!registered.contains(request))throw new IllegalArgumentException("Unknown resource/scope");
        var role=lock(roleId);var cb=em.getCriteriaBuilder();var q=cb.createQuery(RolePermissionEntity.class);var root=q.from(RolePermissionEntity.class);
        q.select(root).where(cb.equal(root.get("namespace"),namespace),cb.equal(root.get("roleId"),roleId),cb.equal(root.get("resource"),request.resource()),cb.equal(root.get("scope"),request.scope()));
        var matches=em.createQuery(q).getResultList();
        var grant=matches.isEmpty()?new RolePermissionEntity():matches.get(0);
        if(matches.isEmpty()){grant.id=UUID.randomUUID().toString();grant.roleId=role.id;grant.namespace=namespace;grant.resource=request.resource();grant.scope=request.scope();grant.effect=effect;em.persist(grant);}else grant.effect=effect;
        em.flush();
    }
    @Transactional
    public void revokePermission(String roleId,PermissionRequest request){
        guard.require(namespace,"revoke-permission",roleId);transaction();lock(roleId);
        var cb=em.getCriteriaBuilder();var q=cb.createQuery(RolePermissionEntity.class);var root=q.from(RolePermissionEntity.class);
        q.select(root).where(cb.equal(root.get("namespace"),namespace),cb.equal(root.get("roleId"),roleId),cb.equal(root.get("resource"),request.resource()),cb.equal(root.get("scope"),request.scope()));
        em.createQuery(q).getResultList().forEach(em::remove);em.flush();
    }
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
        if(!registered.contains(request) || roles.isEmpty())return new FunctionDecision(current,request,false,Set.of());
        var cb=em.getCriteriaBuilder();var q=cb.createQuery(RolePermissionEntity.class);var root=q.from(RolePermissionEntity.class);
        q.select(root).where(root.get("roleId").in(roles),cb.equal(root.get("namespace"),namespace),cb.equal(root.get("resource"),request.resource()),cb.equal(root.get("scope"),request.scope()));
        var grants=em.createQuery(q).getResultList().stream().map(g->new RoleGrant(g.roleId,request,g.effect)).toList();
        return new RolePermissionEvaluator(registered,grants).evaluate(current,request);
    }
    @Transactional(readOnly=true)
    public List<RoleGrant> grants(String roleId){
        guard.require(namespace,"read",roleId);requireRole(roleId);
        var cb=em.getCriteriaBuilder();var q=cb.createQuery(RolePermissionEntity.class);var r=q.from(RolePermissionEntity.class);
        q.select(r).where(cb.equal(r.get("namespace"),namespace),cb.equal(r.get("roleId"),roleId)).orderBy(cb.asc(r.get("resource")),cb.asc(r.get("scope")));
        return em.createQuery(q).getResultList().stream().map(g->new RoleGrant(g.roleId,new PermissionRequest(g.resource,g.scope),g.effect)).toList();
    }
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
