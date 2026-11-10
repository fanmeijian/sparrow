package cn.sparrowmini.admin;
import cn.sparrowmini.rbac.persistence.jpa.catalog.JpaPermissionCatalog;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
/** Catalog methods enforce the dedicated admin.permission-catalog capability. */
@RestController @RequestMapping("/api/permission-catalog")
public class PermissionCatalogController {
 private final JpaPermissionCatalog catalog;private final AdminAccess access;private final cn.sparrowmini.system.audit.AuditService audit;
 public PermissionCatalogController(JpaPermissionCatalog catalog,AdminAccess access,cn.sparrowmini.system.audit.AuditService audit){this.catalog=catalog;this.access=access;this.audit=audit;}
 @GetMapping public Map<String,Object> list(){return Map.of("resources",catalog.resources(),"scopes",catalog.scopes());}
 @PutMapping("/resources") @org.springframework.transaction.annotation.Transactional public JpaPermissionCatalog.Resource resource(@RequestBody JpaPermissionCatalog.Resource input){var result=catalog.saveResource(input);audit.record(access.current().subject(),"UPDATE",input.code(),"SUCCEEDED",java.util.UUID.randomUUID().toString(),result.version(),"PERMISSION_RESOURCE_UPDATED");return result;}
 @PutMapping("/scopes") @org.springframework.transaction.annotation.Transactional public JpaPermissionCatalog.Scope scope(@RequestBody JpaPermissionCatalog.Scope input){var result=catalog.saveScope(input);audit.record(access.current().subject(),"UPDATE",input.resource()+":"+input.code(),"SUCCEEDED",java.util.UUID.randomUUID().toString(),result.version(),"PERMISSION_SCOPE_UPDATED");return result;}
}
