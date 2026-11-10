package cn.sparrowmini.rbac.core;
import cn.sparrowmini.security.api.*;
import java.util.Objects;
public record RoleGrant(String role, PermissionRequest permission, Effect effect) {
 public RoleGrant { if(Objects.requireNonNull(role).isBlank()) throw new IllegalArgumentException("Blank role"); Objects.requireNonNull(permission); Objects.requireNonNull(effect); }
}
