package cn.sparrowmini.security.api;

@FunctionalInterface
public interface CurrentActorProvider { ActorContext current(); }
