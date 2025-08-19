package cn.sparrowmini.common.antlr;
import jakarta.persistence.criteria.*;
import org.antlr.v4.runtime.*;

public class PredicateBuilder {

    public static Predicate buildPredicate(String expression, CriteriaBuilder cb, Root<?> root) {
        CharStream input = CharStreams.fromString(expression);
        ExprLexer lexer = new ExprLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ExprParser parser = new ExprParser(tokens);

        ExprParser.ExprContext tree = parser.expr();
        ExprToPredicateVisitor visitor = new ExprToPredicateVisitor(cb, root);

        return visitor.visit(tree);
    }
}