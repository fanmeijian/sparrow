package cn.sparrowmini.permission.sysrole.repository;

import cn.sparrowmini.permission.sysrole.model.UserSysrole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserSysroleRepository extends JpaRepository<UserSysrole, UserSysrole.UserSysroleId> {
	// @Transactional(propagation=Propagation.NOT_SUPPORTED)
	List<UserSysrole> findByIdUsername(String username);
	Page<UserSysrole> findByIdSysroleId(String sysroleId, Pageable pageable);

//	@Transactional(propagation=Propagation.NOT_SUPPORTED)//解决在检验编辑者权限的时候，与查询在同一个事务造成死循环调用问题。
//	Set<UserSysrole> findByIdUsername(String username);

	@Transactional
	void deleteByIdIn(List<UserSysrole.UserSysroleId> userSysroleIds);

	Page<UserSysrole> findBySysroleCode(String sysroleId, Pageable pageable);
}
