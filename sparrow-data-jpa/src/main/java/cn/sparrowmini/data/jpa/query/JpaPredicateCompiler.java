package cn.sparrowmini.data.jpa.query;

import cn.sparrowmini.core.query.Condition;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import java.util.*;

public final class JpaPredicateCompiler {
    private JpaPredicateCompiler() { }
    public static <T> Specification<T> specification(JpaResource<T> resource, Condition condition) {
        Condition.fields(condition).forEach(resource::requireField);
        return (root, query, cb) -> compile(resource, root, cb, condition, 0);
    }
    public static <T> Predicate compile(JpaResource<T> resource, Root<T> root, CriteriaBuilder cb, Condition c, int depth) {
        if (depth > 64) throw new IllegalArgumentException("Expression nesting limit");
        if (c instanceof Condition.All) return cb.conjunction();
        if (c instanceof Condition.None) return cb.disjunction();
        if (c instanceof Condition.Equal e) {
            Path<?> p = path(resource, root, e.field()); validateType(p, e.value());
            return e.value() == null ? cb.isNull(p) : cb.and(cb.isNotNull(p), cb.equal(p, e.value()));
        }
        if (c instanceof Condition.In e) {
            Path<?> p = path(resource, root, e.field()); e.values().forEach(v -> validateType(p, v));
            return e.values().isEmpty() ? cb.disjunction() : cb.and(cb.isNotNull(p), p.in(e.values()));
        }
        if (c instanceof Condition.Not e) return cb.not(compile(resource, root, cb, e.term(), depth + 1));
        List<Condition> children = c instanceof Condition.And a ? a.terms() : ((Condition.Or)c).terms();
        Predicate[] terms = children.stream().map(t -> compile(resource, root, cb, t, depth + 1)).toArray(Predicate[]::new);
        return c instanceof Condition.And ? cb.and(terms) : cb.or(terms);
    }
    private static <T> Path<?> path(JpaResource<T> r, Root<T> root, String field) {
        r.requireField(field); return root.get(field);
    }
    private static void validateType(Path<?> path, Object value) {
        Class<?> type = path.getJavaType();
        if (type == int.class) type = Integer.class;
        if (type == long.class) type = Long.class;
        if (type == boolean.class) type = Boolean.class;
        if (value != null && !type.isInstance(value)) throw new IllegalArgumentException("Literal type mismatch");
    }
}
