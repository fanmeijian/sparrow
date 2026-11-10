package cn.sparrowmini.identity.local;

import org.springframework.security.core.userdetails.User;
import java.util.List;
/** Serializable session principal; current roles are resolved separately for each permission evaluation. */
public final class LocalPrincipal extends User {
    private static final long serialVersionUID=1L;
    private final String subjectId;
    private final String namespace;
    private final long credentialVersion;
    public LocalPrincipal(String id,String namespace,String username,String password,boolean enabled,long credentialVersion) {
        super(username,password,enabled,true,true,true,List.of());
        this.subjectId=id;this.namespace=namespace;this.credentialVersion=credentialVersion;
    }
    public String subjectId(){return subjectId;}
    public String namespace(){return namespace;}
    public long credentialVersion(){return credentialVersion;}
}
