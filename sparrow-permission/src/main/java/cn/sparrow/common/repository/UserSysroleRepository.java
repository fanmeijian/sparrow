package cn.sparrow.common.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cn.sparrow.model.permission.UserSysrole;
import cn.sparrow.model.permission.UserSysrolePK;

@RepositoryRestResource(exported = false)
public interface UserSysroleRepository extends JpaRepository<UserSysrole, UserSysrolePK> {
	Page<UserSysrole> findByIdUsername(String username,Pageable p);
	
	@RestResource(exported = false)
	@Transactional(propagation=Propagation.NOT_SUPPORTED)//解决在检验编辑者权限的时候，与查询在同一个事务造成死循环调用问题。
	Set<UserSysrole> findByIdUsername(String username);

	@Transactional
	void deleteByIdIn(List<UserSysrolePK> userSysrolePKs);
}
