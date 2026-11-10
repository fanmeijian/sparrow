package cn.sparrowmini.group.core;

import java.util.*;

/** Immutable group membership graph. Parent contains child; role assignments live elsewhere. */
public record GroupGraph(String namespace, Map<String,Node> nodes, Set<Membership> memberships, Set<Edge> edges) {
    public record Node(String id,String name,boolean enabled){public Node{require(id);require(name);}}
    public record Membership(String group,String subject){public Membership{require(group);require(subject);}}
    public record Edge(String parent,String child){public Edge{require(parent);require(child);}}
    public GroupGraph {
        require(namespace);nodes=Map.copyOf(nodes);memberships=Set.copyOf(memberships);edges=Set.copyOf(edges);
        if(nodes.size()>10000 || memberships.size()>100000 || edges.size()>100000)throw new IllegalArgumentException("Group graph capacity exceeded");
        for(var e:nodes.entrySet())if(!e.getKey().equals(e.getValue().id()) || !e.getKey().startsWith(namespace+":"))throw new IllegalArgumentException("Group namespace mismatch");
        for(var m:memberships)if(!nodes.containsKey(m.group()))throw new IllegalArgumentException("Unknown member group");
        Map<String,List<String>> children=new HashMap<>();
        for(var e:edges){if(!nodes.containsKey(e.parent()) || !nodes.containsKey(e.child()))throw new IllegalArgumentException("Unknown group edge");children.computeIfAbsent(e.parent(),k->new ArrayList<>()).add(e.child());}
        Map<String,Integer> lengths=new HashMap<>();
        for(String id:nodes.keySet())depth(id,children,new HashSet<>(),lengths,0);
    }
    public static GroupGraph empty(String namespace){return new GroupGraph(namespace,Map.of(),Set.of(),Set.of());}
    public GroupGraph addGroup(String id,String name){
        if(nodes.containsKey(id))throw new IllegalArgumentException("Duplicate group");
        var next=new HashMap<>(nodes);next.put(id,new Node(id,name,true));return new GroupGraph(namespace,next,memberships,edges);
    }
    public GroupGraph setEnabled(String id,boolean enabled){
        var current=Objects.requireNonNull(nodes.get(id),"Unknown group");var next=new HashMap<>(nodes);
        next.put(id,new Node(id,current.name(),enabled));return new GroupGraph(namespace,next,memberships,edges);
    }
    public GroupGraph addMember(String group,String subject){var next=new HashSet<>(memberships);next.add(new Membership(group,subject));return new GroupGraph(namespace,nodes,next,edges);}
    public GroupGraph removeMember(String group,String subject){var next=new HashSet<>(memberships);next.remove(new Membership(group,subject));return new GroupGraph(namespace,nodes,next,edges);}
    public GroupGraph addChild(String parent,String child){var next=new HashSet<>(edges);next.add(new Edge(parent,child));return new GroupGraph(namespace,nodes,memberships,next);}
    public GroupGraph removeChild(String parent,String child){var next=new HashSet<>(edges);next.remove(new Edge(parent,child));return new GroupGraph(namespace,nodes,memberships,next);}
    public GroupGraph delete(String id){
        if(memberships.stream().anyMatch(m->m.group().equals(id)) || edges.stream().anyMatch(e->e.parent().equals(id)||e.child().equals(id)))throw new IllegalStateException("Group has references");
        var next=new HashMap<>(nodes);if(next.remove(id)==null)throw new IllegalArgumentException("Unknown group");return new GroupGraph(namespace,next,memberships,edges);
    }
    public Set<String> effectiveGroups(String subject){
        require(subject);Set<String> result=new HashSet<>();ArrayDeque<String> queue=new ArrayDeque<>();
        for(var m:memberships)if(m.subject().equals(subject) && nodes.get(m.group()).enabled() && result.add(m.group()))queue.add(m.group());
        Map<String,List<String>> parents=new HashMap<>();for(var e:edges)parents.computeIfAbsent(e.child(),k->new ArrayList<>()).add(e.parent());
        while(!queue.isEmpty())for(String parent:parents.getOrDefault(queue.removeFirst(),List.of()))if(nodes.get(parent).enabled() && result.add(parent))queue.add(parent);
        return Set.copyOf(result);
    }
    private static int depth(String id,Map<String,List<String>> children,Set<String> path,Map<String,Integer> memo,int level){
        if(level>64)throw new IllegalArgumentException("Group nesting too deep");
        if(path.contains(id))throw new IllegalArgumentException("Group nesting cycle");
        if(memo.containsKey(id))return memo.get(id);
        path.add(id);int longest=0;
        for(String child:children.getOrDefault(id,List.of()))longest=Math.max(longest,1+depth(child,children,path,memo,level+1));
        path.remove(id);if(longest>64)throw new IllegalArgumentException("Group nesting too deep");memo.put(id,longest);return longest;
    }
    private static void require(String value){if(value==null || value.isBlank())throw new IllegalArgumentException("Blank group identifier/name");}
}
