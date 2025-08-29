package cn.sparrowmini.bpm.server.antlr;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Root;
import cn.sparrowmini.bpm.server.antlr.ExprBaseVisitor;
import cn.sparrowmini.bpm.server.antlr.ExprParser;
import cn.sparrowmini.bpm.server.antlr.ExprLexer;
import cn.sparrowmini.bpm.server.antlr.ExprParser.ExprContext;
import cn.sparrowmini.bpm.server.antlr.ExprParser.OrExprContext;

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