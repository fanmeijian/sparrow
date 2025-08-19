package cn.sparrowmini.common.repository;

import cn.sparrowmini.common.constant.PermissionTypeEnum;
import cn.sparrowmini.common.model.pem.UserModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserModelRepository extends JpaRepository<UserModel, UserModel.UserModelId> {
	Page<UserModel> findByIdModelId(String modelId, Pageable pageable);

	Page<UserModel> findByIdModelIdAndIdUsername(String modelId, String username, Pageable pageable);

	/**
	 * 是否有配置某类权限
	 * @param modelId
	 * @param permissionType
	 * @return
	 */
	@Query("select count(s)>0 from UserModel s where s.id.permissionType =:permissionType and s.id.modelId=:modelId")
	boolean isConfig(@Param("modelId") String modelId,@Param("permissionType") PermissionTypeEnum permissionType);
}
