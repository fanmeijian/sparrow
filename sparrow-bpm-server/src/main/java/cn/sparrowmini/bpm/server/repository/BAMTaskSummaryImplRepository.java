package cn.sparrowmini.bpm.server.repository;

import org.jbpm.services.task.audit.impl.model.BAMTaskSummaryImpl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BAMTaskSummaryImplRepository extends JpaRepository<BAMTaskSummaryImpl, Long> {
    Optional<BAMTaskSummaryImpl> findByTaskId(Long taskId);
}
