package cn.sparrowmini.common.repository;

import cn.sparrowmini.common.model.ModelAttributeId;
import cn.sparrowmini.common.model.pem.UserModelAttribute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserModelAttributeRepository extends JpaRepository<UserModelAttribute, UserModelAttribute.UserModelAttributeId> {
	Page<UserModelAttribute> findByIdAttributeId(ModelAttributeId attributeId, Pageable pageable);

	Page<UserModelAttribute> findByIdAttributeIdAndIdUsername(ModelAttributeId attributeId, String username,
															  Pageable pageable);
}
