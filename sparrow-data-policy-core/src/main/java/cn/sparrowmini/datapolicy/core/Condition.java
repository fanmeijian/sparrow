package cn.sparrowmini.datapolicy.core;

import java.util.*;

/** Immutable, typed expression tree. All field names are validated by execution adapters. */
public sealed interface Condition {
    boolean matches(Map<String, Object> row);

    record All() implements Condition {
        public boolean matches(Map<String, Object> row) { Objects.requireNonNull(row); return true; }
    }
    record None() implements Condition {
        public boolean matches(Map<String, Object> row) { Objects.requireNonNull(row); return false; }
    }
    record Equal(String field, Object value) implements Condition {
        public Equal { validateField(field); validateLiteral(value); }
        public boolean matches(Map<String, Object> row) {
            return row.containsKey(field) && equalValue(row.get(field), value);
        }
    }
    record In(String field, List<Object> values) implements Condition {
        public In { validateField(field); values = List.copyOf(values); values.forEach(Condition::validateLiteral); }
        public boolean matches(Map<String, Object> row) {
            return row.containsKey(field) && values.stream().anyMatch(v -> equalValue(row.get(field), v));
        }
    }
    record And(List<Condition> terms) implements Condition {
        public And { terms = List.copyOf(terms); }
        public boolean matches(Map<String, Object> row) { return terms.stream().allMatch(t -> t.matches(row)); }
    }
    record Or(List<Condition> terms) implements Condition {
        public Or { terms = List.copyOf(terms); }
        public boolean matches(Map<String, Object> row) { return terms.stream().anyMatch(t -> t.matches(row)); }
    }
    record Not(Condition term) implements Condition {
        public Not { Objects.requireNonNull(term); }
        public boolean matches(Map<String, Object> row) { return !term.matches(row); }
    }
    static Set<String> fields(Condition condition) {
        Set<String> result = new HashSet<>();
        if (condition instanceof Equal c) result.add(c.field());
        else if (condition instanceof In c) result.add(c.field());
        else if (condition instanceof And c) c.terms().forEach(t -> result.addAll(fields(t)));
        else if (condition instanceof Or c) c.terms().forEach(t -> result.addAll(fields(t)));
        else if (condition instanceof Not c) result.addAll(fields(c.term()));
        return Set.copyOf(result);
    }
    private static boolean equalValue(Object left, Object right) {
        if (left instanceof java.math.BigDecimal a && right instanceof java.math.BigDecimal b) return a.compareTo(b) == 0;
        return Objects.equals(left, right);
    }
    private static void validateField(String field) {
        if (Objects.requireNonNull(field).isBlank()) throw new IllegalArgumentException("Blank field");
    }
    private static void validateLiteral(Object value) {
        if (value != null && !(value instanceof String || value instanceof Boolean || value instanceof Integer
                || value instanceof Long || value instanceof java.math.BigDecimal)) {
            throw new IllegalArgumentException("Unsupported literal");
        }
    }
}
