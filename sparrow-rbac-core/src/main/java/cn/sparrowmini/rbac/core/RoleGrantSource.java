package cn.sparrowmini.rbac.core;

import cn.sparrowmini.security.api.PermissionRequest;
import java.util.*;
/** Internal evaluation port, separate from guarded administrative grant inspection. */
@FunctionalInterface
public interface RoleGrantSource { List<RoleGrant> find(Set<String> roleIds,PermissionRequest permission); }
