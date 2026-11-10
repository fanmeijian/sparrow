package cn.sparrowmini.identity.keycloak;

import cn.sparrowmini.identity.api.*;
import org.springframework.http.HttpMethod;
import java.util.*;
import java.nio.charset.StandardCharsets;

/** Keycloak has no JPA-style optimistic version: -1 explicitly identifies unversioned operations. */
public final class KeycloakUserProvider implements UserLifecycleProvider {
    private final KeycloakAdminClient client;
    private final IdentityManagementGuard guard;
    private final String issuer, namespace;
    public KeycloakUserProvider(KeycloakAdminClient client,String issuer,String namespace,IdentityManagementGuard guard){
        this.client=Objects.requireNonNull(client);this.issuer=Objects.requireNonNull(issuer);this.namespace=Objects.requireNonNull(namespace);this.guard=Objects.requireNonNull(guard);
    }
    public String code(){return "keycloak";}
    public UserProfile get(String id){guard.require(namespace,"read",id);return profile(read(localId(id)));}
    public List<UserProfile> list(String search,int offset,int limit){
        guard.require(namespace,"read",null);if(offset<0||limit<1||limit>500)throw new IllegalArgumentException("Bounded pagination required");
        String prefix=Objects.requireNonNullElse(search,"").toLowerCase(Locale.ROOT);
        var output=new ArrayList<UserProfile>();int skipped=0;
        // REST username search is substring-based. Apply the SPI prefix before pagination.
        for(int first=0;first<10000;first+=500){
            var args=new HashMap<String,Object>();args.put("first",first);args.put("max",500);
            if(!prefix.isBlank())args.put("username",prefix);
            var rows=client.request(HttpMethod.GET,List.of("users"),args,null,Map[].class).getBody();
            if(rows==null)throw new IllegalStateException("Missing users response");
            for(var row:rows){var user=profile(row);
                if(user.username()!=null && user.username().toLowerCase(Locale.ROOT).startsWith(prefix)){
                    if(skipped++<offset)continue;output.add(user);if(output.size()==limit)return List.copyOf(output);
                }
            }
            if(rows.length<500)return List.copyOf(output);
        }
        throw new IllegalStateException("User search scan limit exceeded");
    }
    public UserProfile create(String username,String displayName,String password){return create(username,displayName,password,true);}
    public UserProfile createDisabled(String username,String displayName,String password){return create(username,displayName,password,false);}
    private UserProfile create(String username,String displayName,String password,boolean enabled){
        guard.require(namespace,"create",null);text(username);text(password);
        var body=new HashMap<String,Object>();body.put("username",username);body.put("firstName",Objects.requireNonNullElse(displayName,""));body.put("enabled",enabled);
        body.put("credentials",List.of(Map.of("type","password","value",password,"temporary",false)));
        var result=client.request(HttpMethod.POST,List.of("users"),Map.of(),body,Void.class);
        var location=result.getHeaders().getLocation();
        if(location==null || location.getPath()==null)throw new IllegalStateException("Keycloak created user without location");
        String path=location.getPath();String id=path.substring(path.lastIndexOf('/')+1);text(id);
        return profile(read(id));
    }
    public UserProfile update(String id,long expectedVersion,String displayName){
        guard.require(namespace,"update",id);unversioned(expectedVersion);String key=localId(id);
        // Partial update avoids overwriting unrelated concurrent profile fields.
        client.request(HttpMethod.PUT,List.of("users",key),Map.of(),Map.of("firstName",Objects.requireNonNullElse(displayName,"")),Void.class);
        return profile(read(key));
    }
    public UserProfile setEnabled(String id,long expectedVersion,boolean enabled){
        guard.require(namespace,"set-enabled",id);unversioned(expectedVersion);String key=localId(id);
        client.request(HttpMethod.PUT,List.of("users",key),Map.of(),Map.of("enabled",enabled),Void.class);return profile(read(key));
    }
    public void resetPassword(String id,long expectedVersion,String password){
        guard.require(namespace,"reset-password",id);unversioned(expectedVersion);text(password);
        client.request(HttpMethod.PUT,List.of("users",localId(id),"reset-password"),Map.of(),Map.of("type","password","value",password,"temporary",false),Void.class);
    }
    public void logout(String id){guard.require(namespace,"logout",id);client.request(HttpMethod.POST,List.of("users",localId(id),"logout"),Map.of(),null,Void.class);}
    private Map<?,?> read(String id){return Objects.requireNonNull(client.request(HttpMethod.GET,List.of("users",id),Map.of(),null,Map.class).getBody(),"Missing user response");}
    private UserProfile profile(Map<?,?> user){
        String id=(String)user.get("id");text(id);
        return new UserProfile(code(),namespace,KeycloakActorProvider.subject(issuer,id),(String)user.get("username"),(String)user.get("firstName"),Boolean.TRUE.equals(user.get("enabled")),-1);
    }
    private String localId(String id){
        String prefix=KeycloakActorProvider.subject(issuer,"x");prefix=prefix.substring(0,prefix.indexOf('.')+1);
        if(id==null||!id.startsWith(prefix))throw new IllegalArgumentException("Subject outside configured issuer");
        String value=new String(Base64.getUrlDecoder().decode(id.substring(prefix.length())),StandardCharsets.UTF_8);text(value);
        if(!KeycloakActorProvider.subject(issuer,value).equals(id))throw new IllegalArgumentException("Noncanonical subject");return value;
    }
    private static void unversioned(long version){if(version!=-1)throw new IllegalArgumentException("Keycloak update requires explicit unversioned token -1");}
    private static void text(String value){if(value==null||value.isBlank())throw new IllegalArgumentException("Required user value missing");}
}
