package cn.sparrowmini.tree;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
class HierarchyGraphTest {
    HierarchyGraph graph(HierarchyGraph.Mode mode){var g=HierarchyGraph.empty(mode);for(String id:new String[]{"a","b","c","d"})g=g.add(new HierarchyGraph.Node(id,null,null,BigDecimal.ZERO));return g;}
    @Test void treeRejectsMultipleParentsAndCycles(){
        var g=graph(HierarchyGraph.Mode.TREE).link("a","b",BigDecimal.ZERO).link("b","c",BigDecimal.ONE);
        assertThrows(IllegalArgumentException.class,()->g.link("d","c",BigDecimal.ONE));assertThrows(IllegalArgumentException.class,()->g.link("c","a",BigDecimal.ONE));
    }
    @Test void dagSubtreeRejectsExternallySharedDescendants(){
        var g=graph(HierarchyGraph.Mode.DAG).link("a","c",BigDecimal.ZERO).link("b","c",BigDecimal.ZERO);
        assertThrows(IllegalStateException.class,()->g.deleteSubtree("a"));
        var removed=g.detach("b","c").deleteSubtree("a");assertEquals(java.util.Set.of("b","d"),removed.nodes().keySet());
    }
    @Test void moveIsAtomicAndRetainsDecimalOrder(){
        var g=graph(HierarchyGraph.Mode.TREE).link("a","b",BigDecimal.ZERO);
        var moved=g.move("a","c","b",new BigDecimal("0.00000001"));assertEquals("c",moved.edges().iterator().next().parent());
        assertEquals(new BigDecimal("0.00000001"),moved.edges().iterator().next().sortOrder());assertEquals("a",g.edges().iterator().next().parent());
        assertThrows(IllegalArgumentException.class,()->g.move("a","b","b",BigDecimal.ONE));
    }
    @Test void nodeDeletionRequiresExplicitEdgeHandling(){
        var g=graph(HierarchyGraph.Mode.TREE).link("a","b",BigDecimal.ZERO);assertThrows(IllegalStateException.class,()->g.deleteNode("a"));
        assertFalse(g.deleteSubtree("a").nodes().containsKey("b"));assertEquals(4,g.nodes().size());
    }
    @Test void rootOrderingAndDuplicateTargetAreExplicit(){
        var g=graph(HierarchyGraph.Mode.DAG).link("a","c",BigDecimal.ZERO).link("b","c",BigDecimal.ZERO);
        assertThrows(IllegalArgumentException.class,()->g.move("a","b","c",BigDecimal.ZERO));
        var roots=g.detach("a","c").move("b",null,"c",new BigDecimal("-1"));assertEquals("c",roots.roots().get(0).id());
    }
}
