package cn.sparrowmini.admin;

import cn.sparrowmini.security.api.*;
import cn.sparrowmini.identity.api.*;
import cn.sparrowmini.identity.local.*;
import cn.sparrowmini.rbac.persistence.jpa.*;
import cn.sparrowmini.group.persistence.jpa.*;
import cn.sparrowmini.datapolicy.persistence.jpa.*;
import cn.sparrowmini.system.dictionary.*;
import cn.sparrowmini.file.*;
import cn.sparrowmini.storage.api.*;
import cn.sparrowmini.storage.local.*;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import java.util.*;
import java.nio.file.Path;
import java.io.IOException;

@Configuration(proxyBeanMethods=false)
@EnableTransactionManagement(proxyTargetClass=true)
public class AdminServices {
    @Bean Set<PermissionRequest> registeredPermissions(){
        Set<PermissionRequest> result=new HashSet<>();
        for(String resource:List.of("admin.users","admin.roles","admin.groups","admin.policies","admin.dictionaries","admin.files"))
            for(String scope:List.of("read","manage"))result.add(new PermissionRequest(resource,scope));
        for(String scope:List.of("read","download","update","delete"))result.add(new PermissionRequest("file.object",scope));
        return Set.copyOf(result);
    }
    @Bean AdminAccess adminAccess(@Value("${sparrow.identity.namespace:default}")String namespace,ObjectProvider<JpaRoleProvider> roles,ObjectProvider<JpaGroupService> groups,ObjectProvider<LocalIdentityProvider> users){return new AdminAccess(namespace,roles,groups,users);}
    @Bean IdentityManagementGuard identityGuard(AdminAccess access){return (namespace,scope,id)->access.require("admin.users",scope);}
    @Bean JpaRoleProvider roles(EntityManager em,@Value("${sparrow.identity.namespace:default}")String namespace,AdminAccess access,Set<PermissionRequest> registered){return new JpaRoleProvider(em,namespace,(n,op,id)->access.require("admin.roles",op),registered);}
    @Bean JpaGroupService groups(EntityManager em,@Value("${sparrow.identity.namespace:default}")String namespace,AdminAccess access){return new JpaGroupService(em,namespace,(op,before,after)->access.require("admin.groups",op));}
    @Bean JpaPolicyStore policies(EntityManager em,@Value("${sparrow.identity.namespace:default}")String namespace,AdminAccess access){
        var schema=new JpaResource<>("file.object",FileMetadata.class,"id",Set.of("id","name","size","sha256","namespace"));
        Map<PermissionRequest,JpaResource<?>> resources=new HashMap<>();
        for(String scope:List.of("read","download","update","delete"))resources.put(new PermissionRequest("file.object",scope),schema);
        return new JpaPolicyStore(em,namespace,(n,op,p)->access.require("admin.policies",op),resources);
    }
    @Bean DictionaryService dictionaries(EntityManager em,@Value("${sparrow.identity.namespace:default}")String namespace,AdminAccess access){
        // This host has no business reference registry; deletion stays closed until a project supplies one.
        return new DictionaryService(em,namespace,(n,op,id)->access.require("admin.dictionaries",op),(dictionary,entry)->false);
    }
    @Bean StorageRegistry storage(@Value("${sparrow.storage.local.root}")String root,@Value("${sparrow.storage.local.max-bytes:10485760}")long max)throws IOException{
        return new StorageRegistry(List.of(new LocalStorageProvider(Path.of(root),max)));
    }
    @Bean FileReferenceService fileReferences(EntityManager em,@Value("${sparrow.identity.namespace:default}")String namespace,AdminAccess access){
        return new FileReferenceService(em,namespace,(operation,file,resource,record)->{
            access.require("admin.files",operation);
            if(!operation.equals("read"))throw new IllegalStateException("Install a business parent authorization adapter before binding attachments");
        });
    }
    @Bean FileService files(EntityManager em,StorageRegistry storage,@Value("${sparrow.identity.namespace:default}")String namespace,AdminAccess access,FileReferenceService references){
        return new FileService(em,storage,namespace,(n,op,id,context)->{
            if(context!=null && !context.isBlank())throw new IllegalArgumentException("This standalone host has no business attachment adapter");
            access.require("admin.files",Set.of("list","read-metadata","download").contains(op)?"read":"manage");
        },references);
    }
}
