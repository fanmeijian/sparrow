package cn.sparrowmini.core.query;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ConditionTest {
    @Test void rejectsDeepExpressionsWithoutStackOverflow() {
        Condition c = new Condition.All();
        for(int i=0;i<10000;i++) c=new Condition.Not(c);
        Condition deep=c;
        assertThrows(IllegalArgumentException.class,()->Condition.fields(deep));
    }
    @Test void rejectsOversizedExpressions() {
        Condition c=new Condition.And(Collections.nCopies(10001,new Condition.All()));
        assertThrows(IllegalArgumentException.class,()->Condition.fields(c));
    }
    @Test void collectsRegisteredFieldNamesFromNestedExpressions() {
        assertEquals(Set.of("owner","state"),Condition.fields(new Condition.And(List.of(new Condition.Equal("owner","a"),new Condition.Not(new Condition.In("state",List.of("closed")))))));
    }
}
