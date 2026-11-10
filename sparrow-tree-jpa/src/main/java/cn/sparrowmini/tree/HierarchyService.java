package cn.sparrowmini.tree;

import jakarta.persistence.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.math.BigDecimal;
import java.util.function.UnaryOperator;

public class HierarchyService {
    public record Snapshot(String id,String name,long revision,HierarchyGraph graph) { }
    public record Summary(String id,String name,HierarchyGraph.Mode mode,long revision) { }
    @FunctionalInterface public interface Guard{void check(String operation,String hierarchy,HierarchyGraph before,HierarchyGraph after);}
    @FunctionalInterface public interface ReferenceCheck{boolean canDelete(HierarchyGraph.Node node);}
    private final EntityManager em;private final String namespace;private final Guard guard;private final ReferenceCheck references;
    public HierarchyService(EntityManager em,String namespace,Guard guard,ReferenceCheck references){this.em=Objects.requireNonNull(em);this.namespace=Objects.requireNonNull(namespace);this.guard=Objects.requireNonNull(guard);this.references=Objects.requireNonNull(references);}
    @Transactional public Snapshot create(String name,HierarchyGraph.Mode mode){
        transaction();if(name==null||name.isBlank()||name.length()>200)throw new IllegalArgumentException("Invalid hierarchy name");var graph=HierarchyGraph.empty(mode);guard.check("create",null,graph,graph);
        var root=new HierarchyEntity();root.id=UUID.randomUUID().toString();root.namespace=namespace;root.name=name;root.mode=mode;em.persist(root);em.flush();return view(root,graph);
    }
    @Transactional(readOnly=true) public List<Summary> list(int offset,int limit){
        guard.check("read",null,null,null);if(offset<0||limit<1||limit>500)throw new IllegalArgumentException("Invalid pagination");var cb=em.getCriteriaBuilder();var q=cb.createQuery(HierarchyEntity.class);var r=q.from(HierarchyEntity.class);
        q.select(r).where(cb.equal(r.get("namespace"),namespace)).orderBy(cb.asc(r.get("name")),cb.asc(r.get("id")));return em.createQuery(q).setFirstResult(offset).setMaxResults(limit).getResultList().stream().map(h->new Summary(h.id,h.name,h.mode,h.revision)).toList();
    }
    @Transactional public Snapshot get(String id){var root=root(id,LockModeType.PESSIMISTIC_READ);var graph=load(root);guard.check("read",id,graph,graph);return view(root,graph);}
    @Transactional public Snapshot add(String id,long revision,String resource,String recordId,BigDecimal order){return change(id,revision,"add",g->g.add(new HierarchyGraph.Node(UUID.randomUUID().toString(),resource,recordId,order)));}
    @Transactional public Snapshot link(String id,long revision,String parent,String child,BigDecimal order){return change(id,revision,"link",g->g.link(parent,child,order));}
    @Transactional public Snapshot detach(String id,long revision,String parent,String child){return change(id,revision,"detach",g->g.detach(parent,child));}
    @Transactional public Snapshot move(String id,long revision,String oldParent,String newParent,String child,BigDecimal order){return change(id,revision,"move",g->g.move(oldParent,newParent,child,order));}
    @Transactional public Snapshot deleteNode(String id,long revision,String node){return change(id,revision,"delete-node",g->g.deleteNode(node));}
    @Transactional public Snapshot deleteSubtree(String id,long revision,String node){return change(id,revision,"delete-subtree",g->g.deleteSubtree(node));}
    private Snapshot change(String id,long revision,String operation,UnaryOperator<HierarchyGraph> operationFn){
        transaction();var root=root(id,LockModeType.PESSIMISTIC_WRITE);if(root.revision!=revision)throw new OptimisticLockException("Stale hierarchy revision");
        var before=load(root);var after=operationFn.apply(before);guard.check(operation,id,before,after);
        for(var node:before.nodes().values())if(!after.nodes().containsKey(node.id())&&!references.canDelete(node))throw new IllegalStateException("Node has business references");
        sync(root,after);root.revision++;em.flush();return view(root,after);
    }
    private HierarchyEntity root(String id,LockModeType lock){transaction();var root=em.find(HierarchyEntity.class,id,lock);if(root==null||!namespace.equals(root.namespace))throw new IllegalArgumentException("Hierarchy not found");em.refresh(root,lock);return root;}
    private HierarchyGraph load(HierarchyEntity hierarchy){
        Map<String,HierarchyGraph.Node> nodes=new HashMap<>();for(var n:all(HierarchyNodeEntity.class,hierarchy))nodes.put(n.id,new HierarchyGraph.Node(n.id,n.resource,n.recordId,n.rootOrder));
        Set<HierarchyGraph.Edge> edges=new HashSet<>();for(var e:all(HierarchyEdgeEntity.class,hierarchy))edges.add(new HierarchyGraph.Edge(e.parent.id,e.child.id,e.sortOrder));return new HierarchyGraph(hierarchy.mode,nodes,edges);
    }
    private <T>List<T> all(Class<T> type,HierarchyEntity hierarchy){var cb=em.getCriteriaBuilder();var q=cb.createQuery(type);var r=q.from(type);q.select(r).where(cb.equal(r.get("hierarchy"),hierarchy));var rows=em.createQuery(q).setMaxResults(50001).getResultList();if(rows.size()>50000)throw new IllegalStateException("Hierarchy capacity exceeded");return rows;}
    private void sync(HierarchyEntity root,HierarchyGraph graph){
        var existingEdges=all(HierarchyEdgeEntity.class,root);Map<HierarchyGraph.Relation,HierarchyEdgeEntity> edges=new HashMap<>();existingEdges.forEach(e->edges.put(new HierarchyGraph.Relation(e.parent.id,e.child.id),e));
        Set<HierarchyGraph.Relation> wanted=new HashSet<>();graph.edges().forEach(e->wanted.add(new HierarchyGraph.Relation(e.parent(),e.child())));
        existingEdges.stream().filter(e->!wanted.contains(new HierarchyGraph.Relation(e.parent.id,e.child.id))).forEach(em::remove);em.flush();
        Map<String,HierarchyNodeEntity> nodes=new HashMap<>();for(var n:all(HierarchyNodeEntity.class,root)){if(!graph.nodes().containsKey(n.id))em.remove(n);else nodes.put(n.id,n);}
        for(var n:graph.nodes().values()){var entity=nodes.get(n.id());if(entity==null){entity=new HierarchyNodeEntity();entity.id=n.id();entity.hierarchy=root;entity.resource=n.resource();entity.recordId=n.recordId();entity.rootOrder=n.rootOrder();em.persist(entity);nodes.put(n.id(),entity);}else entity.rootOrder=n.rootOrder();}
        for(var e:graph.edges()){var entity=edges.get(new HierarchyGraph.Relation(e.parent(),e.child()));if(entity==null){entity=new HierarchyEdgeEntity();entity.id=UUID.randomUUID().toString();entity.hierarchy=root;entity.parent=nodes.get(e.parent());entity.child=nodes.get(e.child());entity.sortOrder=e.sortOrder();em.persist(entity);}else entity.sortOrder=e.sortOrder();}
    }
    private Snapshot view(HierarchyEntity root,HierarchyGraph graph){return new Snapshot(root.id,root.name,root.revision,graph);}
    private void transaction(){if(!em.isJoinedToTransaction())throw new IllegalStateException("Active transaction required");}
}
