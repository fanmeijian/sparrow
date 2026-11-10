package cn.sparrowmini.rbac.core;

/** Verify stable role references against the configured source, never by username or display name. */
@FunctionalInterface
public interface RoleReferenceValidator { void require(String roleId); }
