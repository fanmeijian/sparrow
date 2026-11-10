package cn.sparrowmini.rbac.keycloak;

import cn.sparrowmini.rbac.core.*;
import cn.sparrowmini.identity.keycloak.*;
import cn.sparrowmini.security.api.Subject;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.representations.idm.RoleRepresentation;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** One configured realm or client role catalog. Effective assignments are fetched per decision. */
public final class KeycloakRoleProvider implements RoleDirectoryProvider,RoleAssignmentProvider,RoleReferenceValidator {
    private final KeycloakAdminClient client;
    private final String issuer,namespace,clientUuid;
    private final RbacManagementGuard guard;
    public KeycloakRoleProvider(KeycloakAdminClient client,String issuer,String namespace,String clientUuid,RbacManagementGuard guard){
        this.client=Objects.requireNonNull(client);this.issuer=Objects.requireNonNull(issuer);this.namespace=Objects.requireNonNull(namespace);this.clientUuid=clientUuid;this.guard=Objects.requireNonNull(guard);
        if(clientUuid!=null && clientUuid.isBlank())throw new IllegalArgumentException("Blank client UUID");
    }
    public String code(){return "keycloak";}
    @Override public Set<RoleCapability> capabilities(){return Set.of(RoleCapability.QUERY,RoleCapability.CREATE);}
    public List<RoleProfile> list(int offset,int limit){
        guard.require(namespace,"read",null);bounded(offset,limit);
        return client.read(r -> catalog(r).list(offset, limit, false)).stream().map(this::profile).toList();
    }
    public RoleProfile create(String code,String name){
        guard.require(namespace,"create",null);
        if(code==null||code.isBlank())throw new IllegalArgumentException("Role name required");
        var role = new RoleRepresentation();
        role.setName(code);
        role.setDescription(Objects.requireNonNullElse(name, ""));
        return client.read(r -> {
            var catalog = catalog(r);
            catalog.create(role);
            return profile(catalog.get(code).toRepresentation());
        });
    }
    public RoleProfile setEnabled(String id,long version,boolean enabled){throw new IllegalArgumentException("Keycloak roles do not support enable/disable");}

    public Set<String> rolesFor(Subject subject){
        String id = subjectId(subject);
        return client.read(r -> {
            if (subject.kind() == Subject.Kind.USER && !Boolean.TRUE.equals(r.users().get(id).toRepresentation().isEnabled())) {
                return Set.of();
            }
            Set<String> roles = new HashSet<>();
            for (var role : scope(r, subject, id).listEffective(false)) {
                validateCatalog(role);
                roles.add(roleId(role.getId()));
            }
            return Set.copyOf(roles);
        });
    }

    public Set<Subject> members(String roleId){
        guard.require(namespace,"read",roleId);
        var role = role(roleId);
        return client.read(r -> {
            var resource = catalog(r).get(role.getName());
            Set<Subject> result = new HashSet<>();
            for (int offset = 0; ; offset += 500) {
                var users = resource.getUserMembers(offset, 500);
                for (var user : users) result.add(new Subject(Subject.Kind.USER, KeycloakActorProvider.subject(issuer, user.getId())));
                if (users.size() < 500) break;
                if (offset >= 9500) throw new IllegalStateException("Role membership limit exceeded");
            }
            for (int offset = 0; ; offset += 500) {
                var groups = resource.getRoleGroupMembers(offset, 500);
                for (var group : groups) result.add(new Subject(Subject.Kind.GROUP, KeycloakActorProvider.subject(issuer, group.getId())));
                if (groups.size() < 500) break;
                if (offset >= 9500) throw new IllegalStateException("Role membership limit exceeded");
            }
            return Set.copyOf(result);
        });
    }
    public void assign(String id,Subject subject){
        guard.require(namespace,"assign-role",id);
        var role = role(id);
        String key = subjectId(subject);
        client.write(r -> scope(r, subject, key).add(List.of(role)));
    }
    public void revoke(String id,Subject subject){
        guard.require(namespace,"revoke-role",id);
        var role = role(id);
        String key = subjectId(subject);
        client.write(r -> scope(r, subject, key).remove(List.of(role)));
    }
    public void require(String id){role(id);}
    private RoleRepresentation role(String id){
        String key = localRole(id);
        var role = client.read(r -> r.rolesById().getRole(key));
        validateCatalog(role);
        return role;
    }
    private void validateCatalog(RoleRepresentation role){
        if(clientUuid==null ? Boolean.TRUE.equals(role.getClientRole()) : !Boolean.TRUE.equals(role.getClientRole()) || !clientUuid.equals(role.getContainerId()))throw new IllegalArgumentException("Role outside configured catalog");
    }
    private RolesResource catalog(RealmResource realm){
        return clientUuid == null ? realm.roles() : realm.clients().get(clientUuid).roles();
    }
    private RoleScopeResource scope(RealmResource realm, Subject subject, String id){
        var mappings = subject.kind() == Subject.Kind.USER ? realm.users().get(id).roles() : realm.groups().group(id).roles();
        return clientUuid == null ? mappings.realmLevel() : mappings.clientLevel(clientUuid);
    }
    private RoleProfile profile(RoleRepresentation role) {
        validateCatalog(role);
        return new RoleProfile(roleId(role.getId()), role.getName(), role.getDescription(), true, -1);
    }
    public String roleId(String key){return "role:"+KeycloakActorProvider.subject(issuer,key);}
    private String localRole(String id){if(id==null||!id.startsWith("role:"))throw new IllegalArgumentException("Qualified role ID required");return localSubject(id.substring(5));}
    private String localSubject(String id){
        String marker=KeycloakActorProvider.subject(issuer,"x");String prefix=marker.substring(0,marker.indexOf('.')+1);
        if(id==null||!id.startsWith(prefix))throw new IllegalArgumentException("Subject outside configured issuer");
        String key=new String(Base64.getUrlDecoder().decode(id.substring(prefix.length())),StandardCharsets.UTF_8);
        if(key.isBlank()||!KeycloakActorProvider.subject(issuer,key).equals(id))throw new IllegalArgumentException("Invalid subject");return key;
    }
    private String subjectId(Subject subject){
        if(subject.kind()==Subject.Kind.ROLE)throw new IllegalArgumentException("Composite role changes require a separate command");
        return localSubject(subject.id());
    }
    private static void bounded(int offset,int limit){if(offset<0||limit<1||limit>500)throw new IllegalArgumentException("Bounded pagination required");}
}
