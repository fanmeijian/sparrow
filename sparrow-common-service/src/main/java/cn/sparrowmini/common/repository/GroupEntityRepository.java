package cn.sparrowmini.common.repository;

import cn.sparrowmini.common.model.pem.group.GroupEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupEntityRepository<T extends GroupEntity> extends JpaRepository<T, GroupEntity.GroupEntityId> {
    Page<T> findByIdGroupId(String groupId, Pageable pageable);
}
