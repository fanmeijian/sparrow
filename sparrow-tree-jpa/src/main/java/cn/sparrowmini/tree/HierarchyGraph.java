package cn.sparrowmini.tree;

import java.math.BigDecimal;
import java.util.*;

public record HierarchyGraph(Mode mode,Map<String,Node> nodes,Set<Edge> edges) {
    public enum Mode { TREE,DAG }
    public record Node(String id,String resource,String recordId,BigDecimal rootOrder){
        public Node{required(id,36);order(rootOrder);if((resource==null)!=(recordId==null))throw new IllegalArgumentException("Incomplete business binding");if(resource!=null){required(resource,100);required(recordId,200);}}
    }
    public record Edge(String parent,String child,BigDecimal sortOrder){public Edge{required(parent,36);required(child,36);order(sortOrder);}}
    public record Relation(String parent,String child) { }
    public HierarchyGraph {
        Objects.requireNonNull(mode);nodes=Map.copyOf(nodes);edges=Set.copyOf(edges);
        if(nodes.size()>10000||edges.size()>50000)throw new IllegalArgumentException("Hierarchy capacity exceeded");
        for(var entry:nodes.entrySet())if(!entry.getKey().equals(entry.getValue().id()))throw new IllegalArgumentException("Node key mismatch");
        Map<String,Integer> incoming=new HashMap<>(),depth=new HashMap<>();Map<String,List<String>> children=new HashMap<>();Set<Relation> unique=new HashSet<>();
        nodes.keySet().forEach(id->{incoming.put(id,0);depth.put(id,0);});
        for(var edge:edges){
            if(!nodes.containsKey(edge.parent())||!nodes.containsKey(edge.child())||edge.parent().equals(edge.child())||!unique.add(new Relation(edge.parent(),edge.child())))throw new IllegalArgumentException("Invalid hierarchy edge");
            int count=incoming.merge(edge.child(),1,Integer::sum);if(mode==Mode.TREE&&count>1)throw new IllegalArgumentException("Tree node has multiple parents");
            children.computeIfAbsent(edge.parent(),id->new ArrayList<>()).add(edge.child());
        }
        ArrayDeque<String> queue=new ArrayDeque<>();incoming.forEach((id,count)->{if(count==0)queue.add(id);});int visited=0;
        while(!queue.isEmpty()){String id=queue.removeFirst();visited++;for(String child:children.getOrDefault(id,List.of())){int level=Math.max(depth.get(child),depth.get(id)+1);if(level>64)throw new IllegalArgumentException("Hierarchy depth limit");depth.put(child,level);if(incoming.merge(child,-1,Integer::sum)==0)queue.add(child);}}
        if(visited!=nodes.size())throw new IllegalArgumentException("Hierarchy cycle");
    }
    public static HierarchyGraph empty(Mode mode){return new HierarchyGraph(mode,Map.of(),Set.of());}
    public HierarchyGraph add(Node node){var next=new HashMap<>(nodes);if(next.putIfAbsent(node.id(),node)!=null)throw new IllegalArgumentException("Duplicate node");return new HierarchyGraph(mode,next,edges);}
    public HierarchyGraph link(String parent,String child,BigDecimal sort){var next=new HashSet<>(edges);if(next.stream().anyMatch(e->e.parent().equals(parent)&&e.child().equals(child)))throw new IllegalArgumentException("Duplicate edge");next.add(new Edge(parent,child,sort));return new HierarchyGraph(mode,nodes,next);}
    public HierarchyGraph detach(String parent,String child){var next=new HashSet<>(edges);if(!next.removeIf(e->e.parent().equals(parent)&&e.child().equals(child)))throw new IllegalArgumentException("Edge not found");return new HierarchyGraph(mode,nodes,next);}
    public HierarchyGraph move(String oldParent,String newParent,String child,BigDecimal sort){
        requireNode(child);var next=new HashSet<>(edges);var nextNodes=new HashMap<>(nodes);
        if(oldParent==null){if(next.stream().anyMatch(e->e.child().equals(child)))throw new IllegalArgumentException("Expected root node");}
        else if(!next.removeIf(e->e.parent().equals(oldParent)&&e.child().equals(child)))throw new IllegalArgumentException("Old edge not found");
        if(newParent!=null){if(next.stream().anyMatch(e->e.parent().equals(newParent)&&e.child().equals(child)))throw new IllegalArgumentException("Target edge already exists");next.add(new Edge(newParent,child,sort));}
        else{if(next.stream().anyMatch(e->e.child().equals(child)))throw new IllegalArgumentException("Detach other parents before moving to roots");var n=nodes.get(child);nextNodes.put(child,new Node(child,n.resource(),n.recordId(),sort));}
        return new HierarchyGraph(mode,nextNodes,next);
    }
    public HierarchyGraph deleteNode(String id){requireNode(id);if(edges.stream().anyMatch(e->e.parent().equals(id)||e.child().equals(id)))throw new IllegalStateException("Node has edges; detach or use subtree command");var next=new HashMap<>(nodes);next.remove(id);return new HierarchyGraph(mode,next,edges);}
    public HierarchyGraph deleteSubtree(String id){
        requireNode(id);Set<String> removed=new HashSet<>();ArrayDeque<String> queue=new ArrayDeque<>();queue.add(id);
        Map<String,List<String>> children=new HashMap<>();edges.forEach(e->children.computeIfAbsent(e.parent(),key->new ArrayList<>()).add(e.child()));
        while(!queue.isEmpty()){String current=queue.removeFirst();if(removed.add(current))queue.addAll(children.getOrDefault(current,List.of()));}
        if(mode==Mode.DAG&&edges.stream().anyMatch(e->removed.contains(e.child())&&!removed.contains(e.parent())))throw new IllegalStateException("Subtree contains externally shared nodes");
        var nextNodes=new HashMap<>(nodes);removed.forEach(nextNodes::remove);var nextEdges=new HashSet<>(edges);nextEdges.removeIf(e->removed.contains(e.parent())||removed.contains(e.child()));return new HierarchyGraph(mode,nextNodes,nextEdges);
    }
    public List<Node> roots(){Set<String> children=new HashSet<>();edges.forEach(e->children.add(e.child()));return nodes.values().stream().filter(n->!children.contains(n.id())).sorted(Comparator.comparing(Node::rootOrder).thenComparing(Node::id)).toList();}
    private void requireNode(String id){if(!nodes.containsKey(id))throw new IllegalArgumentException("Node not found");}
    private static void required(String value,int max){if(value==null||value.isBlank()||value.length()>max)throw new IllegalArgumentException("Invalid hierarchy identifier");}
    private static void order(BigDecimal value){if(value==null||value.precision()>28||Math.abs((long)value.scale())>8)throw new IllegalArgumentException("Invalid sort precision");}
}
