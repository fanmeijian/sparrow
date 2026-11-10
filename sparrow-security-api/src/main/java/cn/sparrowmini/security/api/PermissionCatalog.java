package cn.sparrowmini.security.api;
import java.util.Set;
/** Namespace-bound live permission directory. Disabled definitions remain addressable for cleanup. */
public interface PermissionCatalog {
    Set<PermissionRequest> permissions();
    boolean exists(PermissionRequest permission);
    boolean isEnabled(PermissionRequest permission);
}
