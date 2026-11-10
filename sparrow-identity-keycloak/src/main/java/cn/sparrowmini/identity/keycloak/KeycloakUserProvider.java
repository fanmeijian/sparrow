package cn.sparrowmini.identity.keycloak;

import cn.sparrowmini.identity.api.*;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.admin.client.CreatedResponseUtil;
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
            int pageStart = first;
            var rows = client.read(r -> r.users().search(prefix.isBlank() ? null : prefix, null, null, null, pageStart, 500));
            for(var row:rows){var user=profile(row);
                if(user.username()!=null && user.username().toLowerCase(Locale.ROOT).startsWith(prefix)){
                    if(skipped++<offset)continue;output.add(user);if(output.size()==limit)return List.copyOf(output);
                }
            }
            if(rows.size()<500)return List.copyOf(output);
        }
        throw new IllegalStateException("User search scan limit exceeded");
    }
    public UserProfile create(String username,String displayName,String password){return create(username,displayName,password,true);}
    public UserProfile createDisabled(String username,String displayName,String password){return create(username,displayName,password,false);}
    private UserProfile create(String username,String displayName,String password,boolean enabled){
        guard.require(namespace,"create",null);text(username);text(password);
        var body = new UserRepresentation();
        body.setUsername(username);
        body.setFirstName(Objects.requireNonNullElse(displayName, ""));
        body.setEnabled(enabled);
        body.setCredentials(List.of(credential(password)));
        String id = client.read(r -> {
            try (var response = r.users().create(body)) {
                KeycloakAdminClient.requireSuccess(response);
                return CreatedResponseUtil.getCreatedId(response);
            }
        });
        return profile(read(id));
    }
    public UserProfile update(String id,long expectedVersion,String displayName){
        guard.require(namespace,"update",id);unversioned(expectedVersion);String key=localId(id);
        // Partial update avoids overwriting unrelated concurrent profile fields.
        var patch = new UserRepresentation();
        patch.setFirstName(Objects.requireNonNullElse(displayName, ""));
        client.write(r -> r.users().get(key).update(patch));
        return profile(read(key));
    }
    public UserProfile setEnabled(String id,long expectedVersion,boolean enabled){
        guard.require(namespace,"set-enabled",id);unversioned(expectedVersion);String key=localId(id);
        var patch = new UserRepresentation();
        patch.setEnabled(enabled);
        client.write(r -> r.users().get(key).update(patch));
        return profile(read(key));
    }
    public void resetPassword(String id,long expectedVersion,String password){
        guard.require(namespace,"reset-password",id);unversioned(expectedVersion);text(password);
        String key = localId(id);
        client.write(r -> r.users().get(key).resetPassword(credential(password)));
    }
    public void logout(String id){
        guard.require(namespace,"logout",id);
        String key = localId(id);
        client.write(r -> r.users().get(key).logout());
    }
    private static CredentialRepresentation credential(String password) {
        var credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);
        return credential;
    }
    private UserRepresentation read(String id){
        return client.read(r -> r.users().get(id).toRepresentation());
    }
    private UserProfile profile(UserRepresentation user){
        String id=user.getId();text(id);
        return new UserProfile(code(),namespace,KeycloakActorProvider.subject(issuer,id),user.getUsername(),user.getFirstName(),Boolean.TRUE.equals(user.isEnabled()),-1);
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
