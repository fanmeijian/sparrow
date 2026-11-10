package cn.sparrowmini.web;

import cn.sparrowmini.rbac.core.*;
import cn.sparrowmini.security.api.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/admin/roles")
public class RoleController {
    private final RoleDirectoryProvider roles;
    private final RoleAssignmentProvider assignments;
    private final RolePermissionManager permissions;
    public RoleController(RoleDirectoryProvider roles,RoleAssignmentProvider assignments,RolePermissionManager permissions){
        this.roles=roles;this.assignments=assignments;this.permissions=permissions;
    }
    public record Create(String code,String name) { }
    public record State(long version,boolean enabled) { }
    public record Assignment(Subject.Kind kind,String subject) { }
    public record Grant(String resource,String scope,Effect effect) { }
    @GetMapping("/capabilities") public java.util.Set<RoleCapability> capabilities(){return roles.capabilities();}
    @GetMapping public List<RoleProfile> list(@RequestParam(defaultValue="0")int offset,@RequestParam(defaultValue="50")int limit){return roles.list(offset,limit);}
    @GetMapping("/{id}/members") public java.util.Set<Subject> members(@PathVariable String id){return assignments.members(id);}
    @GetMapping("/{id}/permissions") public List<RoleGrant> grants(@PathVariable String id){return permissions.grants(id);}
    @PostMapping public RoleProfile create(@RequestBody Create input){return roles.create(input.code(),input.name());}
    @PutMapping("/{id}/enabled")public RoleProfile enabled(@PathVariable String id,@RequestBody State input){return roles.setEnabled(id,input.version(),input.enabled());}
    @PostMapping("/{id}/members")public void assign(@PathVariable String id,@RequestBody Assignment input){assignments.assign(id,new Subject(input.kind(),input.subject()));}
    @DeleteMapping("/{id}/members")public void revoke(@PathVariable String id,@RequestBody Assignment input){assignments.revoke(id,new Subject(input.kind(),input.subject()));}
    @PutMapping("/{id}/permissions")public void grant(@PathVariable String id,@RequestBody Grant input){permissions.grant(id,new PermissionRequest(input.resource(),input.scope()),input.effect());}
    @DeleteMapping("/{id}/permissions")public void revokePermission(@PathVariable String id,@RequestBody Grant input){permissions.revokePermission(id,new PermissionRequest(input.resource(),input.scope()));}
}
