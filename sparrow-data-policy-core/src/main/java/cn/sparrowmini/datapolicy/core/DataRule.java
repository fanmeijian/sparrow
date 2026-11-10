package cn.sparrowmini.datapolicy.core;
import java.util.*;
import cn.sparrowmini.security.api.*;
public record DataRule(Subject subject, PermissionRequest permission, Effect effect, Condition condition, Target target, Set<String> fields) {
 public enum Target { ROW, FIELD_READ, FIELD_WRITE }
 public DataRule { Objects.requireNonNull(subject); Objects.requireNonNull(permission); Objects.requireNonNull(effect); Objects.requireNonNull(condition); Objects.requireNonNull(target); fields=Set.copyOf(fields); if(fields.stream().anyMatch(String::isBlank) || (target==Target.ROW && !fields.isEmpty()) || (target!=Target.ROW && fields.isEmpty())) throw new IllegalArgumentException("Invalid fields"); }
}
