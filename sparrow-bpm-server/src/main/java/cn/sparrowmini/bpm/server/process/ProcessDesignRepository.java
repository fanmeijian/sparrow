package cn.sparrowmini.bpm.server.process;

import cn.sparrowmini.bpm.server.repository.ProjectionJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessDesignRepository extends ProjectionJpaRepository<ProcessDesign, ProcessDesign.ProcessDesignId>, JpaRepository<ProcessDesign, ProcessDesign.ProcessDesignId> {
    <T> Page<T> findByContainerId(Container.ContainerId containerId, Pageable pageable);
}
