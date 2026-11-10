package cn.sparrowmini.rbac.core;

import cn.sparrowmini.security.api.*;
/** Effects are provided so removing a DENY can be reviewed as an increase of access. */
@FunctionalInterface
public interface GrantManagementGuard {
    void require(String namespace,String operation,String roleId,PermissionRequest permission,Effect before,Effect after);
}
