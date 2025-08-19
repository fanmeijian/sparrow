package cn.sparrowmini.common.repository;

import cn.sparrowmini.common.model.Scope;

import java.util.Optional;

public interface ScopeRepository extends BaseStateRepository<Scope, String> {
    boolean existsByCode(String scope);

    Scope getReferenceByCode(String scope);

    Optional<Scope> findByCode(String scopeCode);
}
