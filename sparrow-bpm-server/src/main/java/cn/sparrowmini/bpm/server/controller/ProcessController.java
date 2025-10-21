package cn.sparrowmini.bpm.server.controller;

import cn.sparrowmini.bpm.server.common.SparrowTaskInstance;
import cn.sparrowmini.bpm.server.dto.MyApprovedProcess;
import cn.sparrowmini.bpm.server.dto.TaskDataImplDto;
import cn.sparrowmini.bpm.server.dto.TaskDataImplInfo;
import cn.sparrowmini.bpm.server.process.ProcessInstanceLogRepository;
import cn.sparrowmini.bpm.server.repository.AuditTaskImplRepository;
import cn.sparrowmini.bpm.server.repository.TaskImplRepository;
import cn.sparrowmini.bpm.server.util.JsonUtils;
import cn.sparrowmini.bpm.server.util.SparrowCriteriaBuilderHelper;
import lombok.extern.slf4j.Slf4j;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.process.audit.ProcessInstanceLog_;
import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.jbpm.services.api.model.VariableDesc;
import org.jbpm.services.task.audit.impl.model.AuditTaskImpl;
import org.jbpm.services.task.audit.impl.model.AuditTaskImpl_;
import org.jbpm.services.task.impl.model.*;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.internal.query.QueryContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.*;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("processes")
public class ProcessController {
    @Autowired
    private ProcessInstanceLogRepository processInstanceLogRepository;

    @Autowired
    private AuditTaskImplRepository auditTaskImplRepository;

    @Autowired
    private TaskImplRepository taskImplRepository;

    @GetMapping("")
    @ResponseBody
    public Page<Map<String, Object>> getProcessInstanceLogs(Pageable pageable, @RequestParam(value = "variable", required = false) Set<String> variables) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Page<ProcessInstanceLog> processInstanceLogs = processInstanceLogRepository.findByIdentity(username, pageable);
        List<Long> ids = processInstanceLogs.stream().map(ProcessInstanceLog::getProcessInstanceId).collect(Collectors.toList());

        Specification<AuditTaskImpl> filter = auditTaskImplRepository.in(AuditTaskImpl_.PROCESS_INSTANCE_ID, ids)
                .and(
                        auditTaskImplRepository.in(AuditTaskImpl_.STATUS, List.of("Ready"))
                );
        Page<AuditTaskImpl> auditTasks = auditTaskImplRepository.findAll(filter, Pageable.unpaged());

        List<Map<String, Object>> list = JsonUtils.getMapper().convertValue(processInstanceLogs.getContent(), List.class);

        list.forEach(map -> {
            map.put("auditTasks", auditTasks.stream().filter(auditTask -> auditTask.getProcessInstanceId() ==(Long) map.get(ProcessInstanceLog_.PROCESS_INSTANCE_ID)).collect(Collectors.toList()));
        });
        return new PageImpl<>(list, pageable, processInstanceLogs.getTotalElements());
    }


    @GetMapping("/approved-by-me")
    @ResponseBody
    public Page<MyApprovedProcess> approvedByMe(Pageable pageable, @RequestParam(value = "variable", required = false) Set<String> variables) {


        String username = SecurityContextHolder.getContext().getAuthentication().getName();
//        Page<Long> idsPage = processInstanceLogRepository.myAuditTask(username, pageable);
//        List<MyApprovedProcess> processInstanceLogs = processInstanceLogRepository.findApprovedProcesses(idsPage.getContent());
//        return new PageImpl<>(processInstanceLogs, pageable, idsPage.getTotalElements());

        return processInstanceLogRepository.findProcessInstanceLogBy_(username, pageable);
    }

    @GetMapping("/my-tasks")
    @ResponseBody
    public Page<TaskDataImplInfo> myTasks(Pageable pageable, @RequestParam(value = "variable", required = false) Set<String> variables) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Set<String> entityIds = new HashSet<>();
        entityIds.add(username);
        auth.getAuthorities().forEach(f -> {
            entityIds.add(f.getAuthority());
        });
        return taskImplRepository.findTasksWithLatestTitle(entityIds, Set.of("Ready"), pageable);
    }
}
