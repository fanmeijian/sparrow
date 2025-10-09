package cn.sparrowmini.bpm.server.process;

import cn.sparrowmini.bpm.server.dto.MyApprovedProcess;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.process.audit.ProcessInstanceLog_;
import org.jbpm.process.audit.VariableInstanceLog;
import org.jbpm.process.audit.VariableInstanceLog_;
import org.jbpm.services.task.audit.impl.model.AuditTaskImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import javax.persistence.criteria.*;
import java.util.*;

public interface ProcessInstanceLogRepository extends JpaRepository<ProcessInstanceLog, Long>, JpaSpecificationExecutor<ProcessInstanceLog> {
    Optional<ProcessInstanceLog> findByProcessInstanceId(Long processInstanceId);

    Page<ProcessInstanceLog> findByIdentity(String identity, Pageable pageable);

    @Query("select p from ProcessInstanceLog p where p.externalId=:deploymentId and p.processId=:processId and p.processInstanceId=:processInstanceId")
    Optional<ProcessInstanceLog> findProcessInstance(String deploymentId, String processId, Long processInstanceId);

    @Query(value = "select new cn.sparrowmini.bpm.server.dto.MyApprovedProcess(p, v.value) " +
            "        from ProcessInstanceLog p, " +
            "             VariableInstanceLog v " +
            "        where v.id in ( " +
            "            select max(v2.id) " +
            "            from VariableInstanceLog v2 " +
            "            where v2.processInstanceId = p.processInstanceId " +
            "              and v2.variableId = 'title' " +
            "        ) " +
            "        and p.processInstanceId in (:ids) order by p.id desc")
    List<MyApprovedProcess> findApprovedProcesses(Collection<Long> ids);

    @Query("select distinct a.processInstanceId from AuditTaskImpl a where a.actualOwner=:username")
    Page<Long> myAuditTask(String username, Pageable pageable);


    void deleteByProcessInstanceId(Long pid);

}
