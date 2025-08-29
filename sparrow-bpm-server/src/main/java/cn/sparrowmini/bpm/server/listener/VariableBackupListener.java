package cn.sparrowmini.bpm.server.listener;

import cn.sparrowmini.bpm.server.common.VariableArchive;
import cn.sparrowmini.bpm.server.process.VariableArchiveRepository;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.model.ProcessDefinition;
import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessCompletedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class VariableBackupListener extends DefaultProcessEventListener {


    @Autowired
    private VariableArchiveRepository variableArchiveRepository;

    @Autowired
    private RuntimeDataService runtimeDataService;

    @Transactional
    @Override
    public void afterProcessCompleted(ProcessCompletedEvent event) {
        WorkflowProcessInstance pi = (WorkflowProcessInstance) event.getProcessInstance();
        ProcessDefinition processDefinition= this.runtimeDataService.getProcessesByDeploymentIdProcessId(pi.getDeploymentId(), pi.getProcessId());
        Map<String, Object> variables = new HashMap<>();

        processDefinition.getProcessVariables().forEach((k,v)->{
            Object v1= pi.getVariable(k);
            variables.put(k,v1);
        });

        VariableArchive archive = new VariableArchive();
        archive.setId(new VariableArchive.VariableArchiveId(pi.getId(),pi.getProcessId(),pi.getDeploymentId()));
        archive.setCompletedTime(LocalDateTime.now());
        archive.setVariableJson(variables);
        this.variableArchiveRepository.save(archive);

    }
}