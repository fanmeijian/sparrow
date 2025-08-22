package cn.sparrowmini.common.repository;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;

import cn.sparrowmini.common.model.Scope;

public interface ScopeRepository extends BaseStateRepository<Scope, String> {
    boolean existsByCode(String scope);

    Scope getReferenceByCode(String scope);

    Optional<Scope> findByCode(String scopeCode);
    
    
    @Query("select count(*)>0 from UserScope us where us.id.scopeId=:scope or us.id.scopeId=(select id from Scope s where s.id=:scope or s.code=:scope)")
    boolean isConfigUserScope(String scope);
    
    @Query("select count(*)>0 from SysroleScope rs where rs.id.scopeId=:scope or rs.id.scopeId=(select id from Scope s where s.id=:scope or s.code=:scope)")
    boolean isConfigSysroleScope(String scope);
    
    
    @Query("select count(*)>0 from UserScope us where us.id.username=:username and (us.id.scopeId=:scope or us.id.scopeId=(select id from Scope s where s.id=:scope or s.code=:scope))")
    boolean hasUserPermission(String username, String scope);
    
    @Query("select count(*)>0 from SysroleScope rs where rs.id.sysroleId in (:roles) and (rs.id.scopeId=:scope or rs.id.scopeId=(select id from Scope s where s.id=:scope or s.code=:scope))")
    boolean hasSysrolePermission(Collection<String> roles, String scope);
}
