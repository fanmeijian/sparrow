package cn.sparrowmini.security.api;
import java.util.Objects;
public record PermissionRequest(String resource, String scope) {
 public PermissionRequest { if(Objects.requireNonNull(resource).isBlank() || Objects.requireNonNull(scope).isBlank()) throw new IllegalArgumentException("Blank resource/scope"); }
}
