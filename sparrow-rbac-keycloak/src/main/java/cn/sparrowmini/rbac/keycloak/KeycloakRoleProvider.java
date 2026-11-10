package cn.sparrowmini.rbac.keycloak;

import cn.sparrowmini.rbac.core.*;
import cn.sparrowmini.identity.keycloak.*;
import cn.sparrowmini.security.api.Subject;
import org.springframework.http.HttpMethod;
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
        var rows=client.request(HttpMethod.GET,catalog(),Map.of("first",offset,"max",limit),null,Map[].class).getBody();
        return rows==null?List.of():Arrays.stream(rows).map(this::profile).toList();
    }
    public RoleProfile create(String code,String name){
        guard.require(namespace,"create",null);if(code==null||code.isBlank())throw new IllegalArgumentException("Role name required");
        client.request(HttpMethod.POST,catalog(),Map.of(),Map.of("name",code,"description",Objects.requireNonNullElse(name,"")),Void.class);
        var path=new ArrayList<>(catalog());path.add(code);
        return profile(Objects.requireNonNull(client.request(HttpMethod.GET,path,Map.of(),null,Map.class).getBody()));
    }
    public RoleProfile setEnabled(String id,long version,boolean enabled){throw new IllegalArgumentException("Keycloak roles do not support enable/disable");}
    public Set<String> rolesFor(Subject subject){
        var path=subjectPath(subject);
        if(subject.kind()==Subject.Kind.USER){
            var user=Objects.requireNonNull(client.request(HttpMethod.GET,path,Map.of(),null,Map.class).getBody());
            if(!Boolean.TRUE.equals(user.get("enabled")))return Set.of();
        }
        path.add("role-mappings");path.add(clientUuid==null?"realm":"clients");if(clientUuid!=null)path.add(clientUuid);path.add("composite");
        var rows=client.request(HttpMethod.GET,path,Map.of(),null,Map[].class).getBody();
        if(rows==null)throw new IllegalStateException("Missing role mapping response");
        Set<String> roles=new HashSet<>();for(var row:rows){validateCatalog(row);roles.add(roleId((String)row.get("id")));}return Set.copyOf(roles);
    }
    public Set<Subject> members(String roleId){
        guard.require(namespace,"read",roleId);var role=role(roleId);
        Set<Subject> result=new HashSet<>();
        for(String kind:List.of("users","groups")){
            int offset=0;
            while(true){
                var path=new ArrayList<>(catalog());path.add((String)role.get("name"));path.add(kind);
                var rows=client.request(HttpMethod.GET,path,Map.of("first",offset,"max",500),null,Map[].class).getBody();
                if(rows==null)throw new IllegalStateException("Missing role member response");
                for(var row:rows)result.add(new Subject(kind.equals("users")?Subject.Kind.USER:Subject.Kind.GROUP,KeycloakActorProvider.subject(issuer,(String)row.get("id"))));
                offset+=rows.length;if(offset>10000)throw new IllegalStateException("Role membership limit exceeded");if(rows.length<500)break;
            }
        }
        return Set.copyOf(result);
    }
    public void assign(String id,Subject subject){change("assign-role",HttpMethod.POST,id,subject);}
    public void revoke(String id,Subject subject){change("revoke-role",HttpMethod.DELETE,id,subject);}
    private void change(String operation,HttpMethod method,String id,Subject subject){
        guard.require(namespace,operation,id);var role=role(id);var path=subjectPath(subject);path.add("role-mappings");path.add(clientUuid==null?"realm":"clients");if(clientUuid!=null)path.add(clientUuid);
        client.request(method,path,Map.of(),List.of(Map.of("id",role.get("id"),"name",role.get("name"))),Void.class);
    }
    public void require(String id){role(id);}
    private Map<?,?> role(String id){var role=Objects.requireNonNull(client.request(HttpMethod.GET,List.of("roles-by-id",localRole(id)),Map.of(),null,Map.class).getBody());validateCatalog(role);return role;}
    private void validateCatalog(Map<?,?> role){
        if(clientUuid==null ? Boolean.TRUE.equals(role.get("clientRole")) : !Boolean.TRUE.equals(role.get("clientRole")) || !clientUuid.equals(role.get("containerId")))throw new IllegalArgumentException("Role outside configured catalog");
    }
    private List<String> catalog(){return clientUuid==null?List.of("roles"):List.of("clients",clientUuid,"roles");}
    private RoleProfile profile(Map<?,?> role){validateCatalog(role);return new RoleProfile(roleId((String)role.get("id")),(String)role.get("name"),(String)role.get("description"),true,-1);}
    public String roleId(String key){return "role:"+KeycloakActorProvider.subject(issuer,key);}
    private String localRole(String id){if(id==null||!id.startsWith("role:"))throw new IllegalArgumentException("Qualified role ID required");return localSubject(id.substring(5));}
    private String localSubject(String id){
        String marker=KeycloakActorProvider.subject(issuer,"x");String prefix=marker.substring(0,marker.indexOf('.')+1);
        if(id==null||!id.startsWith(prefix))throw new IllegalArgumentException("Subject outside configured issuer");
        String key=new String(Base64.getUrlDecoder().decode(id.substring(prefix.length())),StandardCharsets.UTF_8);
        if(key.isBlank()||!KeycloakActorProvider.subject(issuer,key).equals(id))throw new IllegalArgumentException("Invalid subject");return key;
    }
    private ArrayList<String> subjectPath(Subject subject){
        if(subject.kind()==Subject.Kind.ROLE)throw new IllegalArgumentException("Composite role changes require a separate command");
        return new ArrayList<>(List.of(subject.kind()==Subject.Kind.USER?"users":"groups",localSubject(subject.id())));
    }
    private static void bounded(int offset,int limit){if(offset<0||limit<1||limit>500)throw new IllegalArgumentException("Bounded pagination required");}
}
