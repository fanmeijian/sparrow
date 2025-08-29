package cn.sparrowmini.bpm.server.process;

import org.jbpm.process.audit.ProcessInstanceLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProcessInstanceLogRepository extends JpaRepository<ProcessInstanceLog, Long> {
    Optional<ProcessInstanceLog> findByProcessInstanceId(Long processInstanceId);

    Page<ProcessInstanceLog> findByIdentity(String identity, Pageable pageable);

    @Query("select p from ProcessInstanceLog p where p.externalId=:deploymentId and p.processId=:processId and p.processInstanceId=:processInstanceId")
    Optional<ProcessInstanceLog> findProcessInstance(String deploymentId, String processId, Long processInstanceId);

    void deleteByProcessInstanceId(Long pid);
}
