package cn.sparrowmini.common.repository;

import cn.sparrowmini.common.model.AppConfigAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

public interface AppConfigAttachmentRepository extends BaseStateRepository<AppConfigAttachment, String> {

    @Transactional
    @Modifying
    @Query("delete from AppConfigAttachment a where a.id in (:ids)")
    void deleteByIds( Set<String> ids);
}
