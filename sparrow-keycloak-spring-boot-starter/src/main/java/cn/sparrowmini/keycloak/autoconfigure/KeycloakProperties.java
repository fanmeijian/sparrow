package cn.sparrowmini.keycloak.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.*;
@ConfigurationProperties("sparrow.keycloak")
public class KeycloakProperties {
    private String issuer,serverUrl,realm,audience,managementClientId,managementClientSecret,roleClientUuid;
    private String namespace="default";
    private Set<String> roleClients=Set.of();
    private boolean managementEnabled;
    public String getIssuer(){return issuer;} public void setIssuer(String v){issuer=v;}
    public String getServerUrl(){return serverUrl;} public void setServerUrl(String v){serverUrl=v;}
    public String getRealm(){return realm;} public void setRealm(String v){realm=v;}
    public String getAudience(){return audience;} public void setAudience(String v){audience=v;}
    public String getNamespace(){return namespace;} public void setNamespace(String v){namespace=v;}
    public Set<String> getRoleClients(){return roleClients;} public void setRoleClients(Set<String> v){roleClients=Set.copyOf(v);}
    public boolean isManagementEnabled(){return managementEnabled;} public void setManagementEnabled(boolean v){managementEnabled=v;}
    public String getManagementClientId(){return managementClientId;} public void setManagementClientId(String v){managementClientId=v;}
    public String getManagementClientSecret(){return managementClientSecret;} public void setManagementClientSecret(String v){managementClientSecret=v;}
    public String getRoleClientUuid(){return roleClientUuid;} public void setRoleClientUuid(String v){roleClientUuid=v;}
}
