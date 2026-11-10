package cn.sparrowmini.group.core;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
class GroupGraphTest {
    GroupGraph graph(){return GroupGraph.empty("tenant").addGroup("tenant:parent","Parent").addGroup("tenant:child","Child").addGroup("tenant:other","Other");}
    @Test void childMembersBelongToParentButNotConversely(){
        var g=graph().addChild("tenant:parent","tenant:child").addMember("tenant:child","alice").addMember("tenant:parent","bob");
        assertEquals(Set.of("tenant:parent","tenant:child"),g.effectiveGroups("alice"));assertEquals(Set.of("tenant:parent"),g.effectiveGroups("bob"));
    }
    @Test void disabledPathStopsInheritanceButOtherPathsRemain(){
        var g=graph().addChild("tenant:parent","tenant:child").addChild("tenant:other","tenant:child").addMember("tenant:child","alice");
        assertEquals(Set.of("tenant:child","tenant:other"),g.setEnabled("tenant:parent",false).effectiveGroups("alice"));
        assertEquals(Set.of(),g.setEnabled("tenant:child",false).effectiveGroups("alice"));
    }
    @Test void loopsCrossNamespacesAndOrphansFail(){
        var g=graph().addChild("tenant:parent","tenant:child");
        assertThrows(IllegalArgumentException.class,()->g.addChild("tenant:child","tenant:parent"));
        assertThrows(IllegalArgumentException.class,()->g.addChild("tenant:child","tenant:child"));
        assertThrows(IllegalArgumentException.class,()->g.addGroup("other:group","Other"));
        assertThrows(IllegalArgumentException.class,()->g.addMember("tenant:missing","alice"));
    }
    @Test void mutationIsImmutableAndDeletionChecksReferences(){
        var original=graph();var changed=original.addMember("tenant:child","alice");
        assertTrue(original.effectiveGroups("alice").isEmpty());
        assertThrows(IllegalStateException.class,()->changed.delete("tenant:child"));
        assertFalse(changed.removeMember("tenant:child","alice").delete("tenant:child").nodes().containsKey("tenant:child"));
    }
}
