package cn.sparrowmini.rbac.core;

import cn.sparrowmini.security.api.Subject;
import java.util.Set;
public interface RoleAssignmentProvider {
    Set<String> rolesFor(Subject subject);
    void assign(String roleId,Subject subject);
    void revoke(String roleId,Subject subject);
}
