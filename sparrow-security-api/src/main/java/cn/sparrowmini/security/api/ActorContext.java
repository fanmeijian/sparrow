package cn.sparrowmini.security.api;
import java.util.Set;
import java.util.Objects;
/** Trusted identity snapshot; all identifiers are namespace-qualified by the host. */
public record ActorContext(String subject, Set<String> roles, Set<String> groups) {
 public ActorContext { if (Objects.requireNonNull(subject).isBlank()) throw new IllegalArgumentException("Blank subject"); roles=Set.copyOf(roles); groups=Set.copyOf(groups); }
}
