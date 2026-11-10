package cn.sparrowmini.security.api;

@FunctionalInterface
public interface FunctionPermissionEvaluator {
    FunctionDecision evaluate(ActorContext actor,PermissionRequest request);
    default boolean supportsRoleEvidence(){return false;}
}
