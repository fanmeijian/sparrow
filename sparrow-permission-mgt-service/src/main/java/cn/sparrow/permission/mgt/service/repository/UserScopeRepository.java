package cn.sparrow.permission.mgt.service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import cn.sparrow.permission.model.resource.UserScope;
import cn.sparrow.permission.model.resource.UserScopePK;

public interface UserScopeRepository extends JpaRepository<UserScope, UserScopePK> {

	Page<UserScope> findByIdScopeId(String scopeId, Pageable pageable);

}
