package cn.sparrow.permission.repository.organization;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import cn.sparrow.permission.model.organization.PositionLevel;

public interface PositionLevelRepository extends JpaRepository<PositionLevel, String> {

	@Transactional
	void deleteByIdIn(String[] ids);

	Iterable<PositionLevel> findByIsRoot(boolean b);
}
