package cn.sparrowmini.security.api;
import java.util.Set;
import java.util.Objects;
/** Server-side result, not a client-supplied authorization token. */
public record FunctionDecision(ActorContext actor, PermissionRequest request, boolean allowed, Set<String> eligibleRoles) {
 public FunctionDecision { Objects.requireNonNull(actor); Objects.requireNonNull(request); eligibleRoles=Set.copyOf(eligibleRoles); if(!actor.roles().containsAll(eligibleRoles) || (!allowed && !eligibleRoles.isEmpty())) throw new IllegalArgumentException("Invalid role evidence"); }
}
