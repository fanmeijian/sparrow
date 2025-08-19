package cn.sparrowmini.common.repository;

import cn.sparrowmini.common.model.BaseFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseFileRepository<S extends BaseFile, ID> extends BaseOpLogRepository<S, ID> {
}
