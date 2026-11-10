package cn.sparrowmini.group.core;

/** Checks management permission and downstream delegation effects, including removal of DENY memberships. */
@FunctionalInterface
public interface GroupChangeGuard {void check(String operation,GroupGraph before,GroupGraph after);}
