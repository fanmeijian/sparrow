package cn.sparrowmini.rbac.core;

import cn.sparrowmini.security.api.Effect;
import cn.sparrowmini.security.api.PermissionRequest;

/** Functional grants use stable role IDs, independently of the identity provider. */
public interface RolePermissionManager {
    void grant(String roleId, PermissionRequest request, Effect effect);
    void revokePermission(String roleId, PermissionRequest request);
}
