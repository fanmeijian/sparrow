package cn.sparrowmini.bpm.server.process.repository;

import org.jbpm.services.task.audit.impl.model.AuditTaskImpl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface AuditTaskRepository extends JpaRepository<AuditTaskImpl, Long> {
    void deleteByProcessInstanceId(long processInstanceId);

    List<AuditTaskImpl> findByProcessInstanceId(Long pid);
}
