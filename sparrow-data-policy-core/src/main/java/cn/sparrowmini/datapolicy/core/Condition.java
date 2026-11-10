package cn.sparrowmini.datapolicy.core;
import java.util.*;
/** Structured conditions: absence is distinct from explicit null; no executable scripts. */
public sealed interface Condition permits Condition.All, Condition.Equal, Condition.And {
 boolean matches(Map<String,Object> row);
 record All() implements Condition { public boolean matches(Map<String,Object> row) { Objects.requireNonNull(row); return true; } }
 record Equal(String field, Object value) implements Condition {
  public Equal { if(Objects.requireNonNull(field).isBlank()) throw new IllegalArgumentException("Blank field"); if(value!=null && !(value instanceof String || value instanceof Boolean || value instanceof Integer || value instanceof Long || value instanceof java.math.BigDecimal)) throw new IllegalArgumentException("Unsupported literal"); }
  public boolean matches(Map<String,Object> row) { return row.containsKey(field) && Objects.equals(row.get(field),value); }
 }
 record And(List<Condition> terms) implements Condition {
  public And { terms=List.copyOf(terms); }
  public boolean matches(Map<String,Object> row) { return terms.stream().allMatch(t -> t.matches(row)); }
 }
}
