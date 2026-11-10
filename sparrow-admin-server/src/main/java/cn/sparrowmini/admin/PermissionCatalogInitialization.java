package cn.sparrowmini.admin;
import cn.sparrowmini.rbac.persistence.jpa.catalog.JpaPermissionCatalog;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.List;
/** Seeds built-in capabilities only when absent; never overwrites operator changes. */
@Component @Order(org.springframework.core.Ordered.HIGHEST_PRECEDENCE)
public class PermissionCatalogInitialization implements ApplicationRunner {
 private final JpaPermissionCatalog catalog;private final AdminAccess access;private final TransactionTemplate tx;
 public PermissionCatalogInitialization(JpaPermissionCatalog catalog,AdminAccess access,PlatformTransactionManager manager){this.catalog=catalog;this.access=access;this.tx=new TransactionTemplate(manager);}
 public void run(ApplicationArguments args){access.initialize(()->tx.executeWithoutResult(status->{
  for(String resource:List.of("admin.users","admin.roles","admin.groups","admin.policies","admin.dictionaries","admin.files","admin.audit","admin.ui-pages","admin.configurations","admin.navigation","admin.trees","admin.permission-catalog","file.object")){
   if(catalog.resources().stream().noneMatch(r->r.code().equals(resource)))catalog.saveResource(new JpaPermissionCatalog.Resource(resource,resource,"Built-in resource",true,-1));
   for(String scope:resource.equals("file.object")?List.of("read","download","update","delete"):List.of("read","manage"))
    if(!catalog.exists(new cn.sparrowmini.security.api.PermissionRequest(resource,scope)))catalog.saveScope(new JpaPermissionCatalog.Scope(resource,scope,scope,"Built-in operation",true,-1));
  }
 }));}
}
