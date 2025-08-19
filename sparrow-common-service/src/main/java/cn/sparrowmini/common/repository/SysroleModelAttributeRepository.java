package cn.sparrowmini.common.repository;

import cn.sparrowmini.common.constant.PermissionEnum;
import cn.sparrowmini.common.constant.PermissionTypeEnum;
import cn.sparrowmini.common.model.ModelAttributeId;
import cn.sparrowmini.common.model.pem.SysroleModelAttribute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SysroleModelAttributeRepository extends JpaRepository<SysroleModelAttribute, SysroleModelAttribute.SysroleModelAttributeId> {
	Page<SysroleModelAttribute> findByIdAttributeId(ModelAttributeId attributeId, Pageable pageable);

	Page<SysroleModelAttribute> findByIdAttributeIdAndIdSysroleId(ModelAttributeId attributeId, String sysroleId,
																  Pageable pageable);
	
	int countByIdAttributeIdAndIdPermissionAndIdPermissionType(ModelAttributeId attributeId, PermissionEnum permission,
															   PermissionTypeEnum type);

	@Query("select s from SysroleModelAttribute s where s.id.attributeId =:attributeId")
	List<SysroleModelAttribute> getByUserName(ModelAttributeId attributeId, String username);
}
