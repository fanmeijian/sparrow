package cn.sparrowmini.bpm.server.repository;

import org.jbpm.process.audit.NodeInstanceLog;
import org.jbpm.services.task.audit.impl.model.AuditTaskImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AuditTaskImplRepository extends BaseRepository<AuditTaskImpl, Integer> {
//    @Query("select t from AuditTaskImpl t where t.taskId in (" +
//            "select PepleAssingment)")
//    Page<AuditTaskImpl> myTask(Integer processInstanceId, Pageable pageable);

    Optional<AuditTaskImpl> findByTaskId(Long taskId);

}
