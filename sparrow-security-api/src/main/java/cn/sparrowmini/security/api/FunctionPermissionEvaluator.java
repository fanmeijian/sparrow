package cn.sparrowmini.security.api;
public interface FunctionPermissionEvaluator { FunctionDecision evaluate(ActorContext actor, PermissionRequest request); }
