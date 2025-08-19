package cn.sparrowmini.common.repository;


import cn.sparrowmini.common.constant.PermissionEnum;
import cn.sparrowmini.common.constant.PermissionTypeEnum;
import cn.sparrowmini.common.model.pem.SysroleModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SysroleModelRepository extends JpaRepository<SysroleModel, SysroleModel.SysroleModelId> {
	Page<SysroleModel> findByIdModelId(String modelId, Pageable pageable);

	Page<SysroleModel> findByIdModelIdAndIdSysroleId(String modelId, String sysroleId, Pageable pageable);
	
	int countByIdModelIdAndIdPermissionAndIdPermissionType(String modelId, PermissionEnum permission, PermissionTypeEnum type);

	/**
	 * 判断是否存在某个权限
	 * @param modelId
	 * @param permissionType
	 * @param username
	 * @return
	 */
	@Query("select count(s)>0 from SysroleModel s where s.id.permissionType =:permissionType and s.id.modelId =:modelId")
	boolean isPermission(@Param("modelId") String modelId, @Param("permissionType") PermissionTypeEnum permissionType, @Param("username") String username);

	/**
	 * 是否有配置某类权限
	 * @param modelId
	 * @param permissionType
	 * @return
	 */
	@Query("select count(s)>0 from SysroleModel s where s.id.permissionType =:permissionType and s.id.modelId=:modelId")
	boolean isConfig(@Param("modelId") String modelId,@Param("permissionType") PermissionTypeEnum permissionType);

}
