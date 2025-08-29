package cn.sparrowmini.bpm.server.process.repository;

import org.jbpm.process.audit.VariableInstanceLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VariableInstanceLogRepository extends JpaRepository<VariableInstanceLog, Long> {
    void deleteByProcessInstanceId(Long pid);
}
