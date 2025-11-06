package cn.sparrowmini.bpm.server.process;

import org.jbpm.process.audit.NodeInstanceLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface NodeInstanceLogRepository extends JpaRepository<NodeInstanceLog, Long> {
    Page<NodeInstanceLog> findByProcessInstanceId(long processInstanceId, Pageable pageable);

    void deleteByProcessInstanceId(Long pid);

    @Query("select n from NodeInstanceLog n where n.processInstanceId=:processInstanceId and n.nodeInstanceId=:nodeInstanceId and n.type=:type")
    Optional<NodeInstanceLog> findByNodeInstanceId(Long processInstanceId, String nodeInstanceId, Integer type);


    @Query("select n from NodeInstanceLog n left join AuditTaskImpl a on n.workItemId=a.workItemId where n.type=0 and a.taskId=:taskId")
    Optional<NodeInstanceLog> getNodeInstanceId(Long taskId);
}
