package cn.sparrowmini.group.persistence.jpa;

import cn.sparrowmini.group.core.*;
import jakarta.persistence.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.function.UnaryOperator;

public class JpaGroupService implements cn.sparrowmini.security.api.GroupMembershipResolver {
    public record Snapshot(long revision,GroupGraph graph) { }
    private final EntityManager em;private final String namespace;private final GroupChangeGuard guard;
    public JpaGroupService(EntityManager em,String namespace,GroupChangeGuard guard){
        this.em=Objects.requireNonNull(em);this.namespace=Objects.requireNonNull(namespace);this.guard=Objects.requireNonNull(guard);
        if(!namespace.matches("[a-zA-Z0-9_-]{1,100}"))throw new IllegalArgumentException("Invalid namespace");
    }
    @Transactional public void initialize(){
        transaction();var empty=GroupGraph.empty(namespace);guard.check("initialize",empty,empty);
        if(em.find(GroupNamespace.class,namespace)==null){var root=new GroupNamespace();root.id=namespace;em.persist(root);em.flush();}
    }
    /** Internal fact snapshot; all mutations serialize against this namespace row. */
    @Transactional public Snapshot facts(){
        var root=locked(LockModeType.PESSIMISTIC_READ);return new Snapshot(root.revision,load());
    }
    @Transactional public Set<String> groupsFor(String subject){return facts().graph().effectiveGroups(subject);}
    @Transactional public Snapshot inspect(){var snapshot=facts();guard.check("read",snapshot.graph(),snapshot.graph());return snapshot;}
    @Transactional public Snapshot create(long revision,String code,String name){
        if(!code.matches("[a-zA-Z0-9_.-]{1,100}") || name.length()>200)throw new IllegalArgumentException("Invalid group");
        return change(revision,"create",g->g.addGroup(namespace+":"+code,name));
    }
    @Transactional public Snapshot addMember(long revision,String group,String subject){return change(revision,"manage-members",g->g.addMember(group,subject));}
    @Transactional public Snapshot removeMember(long revision,String group,String subject){return change(revision,"manage-members",g->g.removeMember(group,subject));}
    @Transactional public Snapshot addChild(long revision,String parent,String child){return change(revision,"manage-nesting",g->g.addChild(parent,child));}
    @Transactional public Snapshot removeChild(long revision,String parent,String child){return change(revision,"manage-nesting",g->g.removeChild(parent,child));}
    @Transactional public Snapshot setEnabled(long revision,String id,boolean enabled){return change(revision,"disable",g->g.setEnabled(id,enabled));}
    @Transactional public Snapshot delete(long revision,String id){return change(revision,"delete",g->g.delete(id));}
    private Snapshot change(long revision,String operation,UnaryOperator<GroupGraph> change){
        transaction();var root=locked(LockModeType.PESSIMISTIC_WRITE);
        if(root.revision!=revision)throw new OptimisticLockException("Stale membership revision");
        var before=load();var after=change.apply(before);guard.check(operation,before,after);
        sync(before,after);root.revision++;em.flush();return new Snapshot(root.revision,after);
    }
    private GroupNamespace locked(LockModeType mode){transaction();var root=em.find(GroupNamespace.class,namespace,mode);if(root==null)throw new IllegalStateException("Namespace not initialized");em.refresh(root,mode);return root;}
    private GroupGraph load(){
        Map<String,GroupGraph.Node> nodes=new HashMap<>();for(var n:all(GroupNodeEntity.class))nodes.put(n.id,new GroupGraph.Node(n.id,n.name,n.enabled));
        Set<GroupGraph.Membership> members=new HashSet<>();for(var m:all(GroupMemberEntity.class))members.add(new GroupGraph.Membership(m.groupId,m.subjectId));
        Set<GroupGraph.Edge> edges=new HashSet<>();for(var e:all(GroupEdgeEntity.class))edges.add(new GroupGraph.Edge(e.parentId,e.childId));
        return new GroupGraph(namespace,nodes,members,edges);
    }
    private void sync(GroupGraph before,GroupGraph after){
        for(var entity:all(GroupMemberEntity.class))if(!after.memberships().contains(new GroupGraph.Membership(entity.groupId,entity.subjectId)))em.remove(entity);
        for(var entity:all(GroupEdgeEntity.class))if(!after.edges().contains(new GroupGraph.Edge(entity.parentId,entity.childId)))em.remove(entity);
        for(var entity:all(GroupNodeEntity.class)){var node=after.nodes().get(entity.id);if(node==null)em.remove(entity);else{entity.name=node.name();entity.enabled=node.enabled();}}
        for(var node:after.nodes().values())if(!before.nodes().containsKey(node.id())){var n=new GroupNodeEntity();n.id=node.id();n.namespace=namespace;n.name=node.name();n.enabled=node.enabled();em.persist(n);}
        for(var m:after.memberships())if(!before.memberships().contains(m)){if(m.subject().length()>300)throw new IllegalArgumentException("Subject ID too long");var n=new GroupMemberEntity();n.id=UUID.randomUUID().toString();n.namespace=namespace;n.groupId=m.group();n.subjectId=m.subject();em.persist(n);}
        for(var e:after.edges())if(!before.edges().contains(e)){var n=new GroupEdgeEntity();n.id=UUID.randomUUID().toString();n.namespace=namespace;n.parentId=e.parent();n.childId=e.child();em.persist(n);}
    }
    private <T>List<T> all(Class<T> type){var cb=em.getCriteriaBuilder();var q=cb.createQuery(type);var root=q.from(type);q.select(root).where(cb.equal(root.get("namespace"),namespace));var rows=em.createQuery(q).setMaxResults(100001).getResultList();if(rows.size()>100000)throw new IllegalStateException("Group capacity exceeded");return rows;}
    private void transaction(){if(!em.isJoinedToTransaction())throw new IllegalStateException("Active transaction required");}
}
