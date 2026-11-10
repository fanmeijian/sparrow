package cn.sparrowmini.rbac.core;

/** Host checks both management scope and delegation boundary for the affected role. */
@FunctionalInterface
public interface RbacManagementGuard { void require(String namespace,String operation,String roleId); }
