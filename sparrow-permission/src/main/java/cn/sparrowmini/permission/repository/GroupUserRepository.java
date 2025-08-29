package cn.sparrowmini.permission.repository;

import cn.sparrowmini.permission.model.relation.GroupUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupUserRepository extends JpaRepository<GroupUser, GroupUser.GroupUserId> {

	List<GroupUser> findByIdUsername(String username);

	Page<GroupUser> findByIdGroupId(String groupId, Pageable pageable);
	
	void deleteByIdGroupId(String groupId);

}
