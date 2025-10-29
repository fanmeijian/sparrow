package cn.sparrowmini.bpm.server.process.repository;

import org.jbpm.process.audit.VariableInstanceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface VariableInstanceLogRepository extends JpaRepository<VariableInstanceLog, Long> {
    void deleteByProcessInstanceId(Long pid);

    @Query("select max(v) from VariableInstanceLog v where v.processInstanceId in (:pids) and v.variableInstanceId='title' group by v.processInstanceId")
    List<VariableInstanceLog> findByProcessInstanceId(Collection<Long> pids);
}
