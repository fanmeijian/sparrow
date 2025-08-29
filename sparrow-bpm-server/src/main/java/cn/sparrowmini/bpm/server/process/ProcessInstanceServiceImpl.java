package cn.sparrowmini.bpm.server.process;

import cn.sparrowmini.bpm.server.process.repository.AuditTaskRepository;
import cn.sparrowmini.bpm.server.process.repository.ProcessInstanceInfoRepository;
import cn.sparrowmini.bpm.server.process.repository.TaskRepository;
import cn.sparrowmini.bpm.server.process.repository.VariableInstanceLogRepository;
import org.jbpm.process.audit.AuditLogService;
import org.jbpm.process.audit.NodeInstanceLog;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.UserTaskService;
import org.jbpm.services.api.model.NodeInstanceDesc;
import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.jbpm.services.task.audit.impl.model.AuditTaskImpl;
import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.jbpm.workflow.instance.node.WorkItemNodeInstance;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.kie.server.remote.rest.jbpm.admin.ProcessAdminResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.kie.api.runtime.query.QueryContext;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProcessInstanceServiceImpl implements ProcessInstanceService {

    @Autowired
    private RuntimeDataService runtimeDataService;

    @Autowired
    private DeploymentService deploymentService;

    @Autowired
    private ProcessService processInstanceService;

    @Autowired
    private NodeInstanceLogRepository nodeInstanceLogRepository;
    @Autowired
    private ProcessInstanceLogRepository processInstanceLogRepository;

    @Autowired
    private AuditTaskRepository auditTaskRepository;

    @Autowired
    private ProcessInstanceInfoRepository processInstanceInfoRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private VariableInstanceLogRepository variableInstanceLogRepository;
    @Autowired
    private UserTaskService userTaskService;

    @Override
    public Page<ProcessInstanceLog> allProcessInstance(Pageable pageable) {
        return this.processInstanceLogRepository.findAll(pageable);

    }

    @Override
    public Page<ProcessInstanceLog> MyProcessInstances(Pageable pageable) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return this.processInstanceLogRepository.findByIdentity(username,pageable);
    }

    @Override
    public Page<NodeInstanceLog> getNodeInstanceByProcessInstanceId(Long processInstanceId, Pageable pageable) {
        return nodeInstanceLogRepository.findByProcessInstanceId(processInstanceId, pageable);
    }

    @Override
    public ProcessInstanceLog getProcessInstance(Long processInstanceId) {
        return processInstanceLogRepository.findByProcessInstanceId(processInstanceId).orElseThrow();
    }

    @Transactional
    @Override
    public void triggerNode(String containerId, Long processInstanceId, Long nodeInstanceId) {
        RuntimeManager runtimeManager = deploymentService.getRuntimeManager(containerId);
        RuntimeEngine engine = runtimeManager.getRuntimeEngine(ProcessInstanceIdContext.get(processInstanceId));
        KieSession kieSession = engine.getKieSession();

        // 运行时的流程实例（可以强转）
        WorkflowProcessInstance wpi = (WorkflowProcessInstance) kieSession.getProcessInstance(processInstanceId);

        // 获取所有当前激活的 NodeInstance
        Collection<NodeInstance> nodeInstances = wpi.getNodeInstances();
        for (NodeInstance nodeInstance : nodeInstances) {
            if (nodeInstance instanceof WorkItemNodeInstance) {
                WorkItemNodeInstance win = (WorkItemNodeInstance) nodeInstance;
                System.out.println("节点名称: " + win.getNodeName());
                win.cancel();
            }
        }
    }

    @Transactional
    @Override
    public void deleteProcessInstance(Set<Long> processInstanceIds) {
        processInstanceIds.forEach(pid->{
            List<AuditTaskImpl> auditTaskList = auditTaskRepository.findByProcessInstanceId(pid);
            taskRepository.deleteAllById(auditTaskList.stream().map(AuditTaskImpl::getTaskId).collect(Collectors.toList()));
            auditTaskRepository.deleteByProcessInstanceId(pid);
            variableInstanceLogRepository.deleteByProcessInstanceId(pid);
            nodeInstanceLogRepository.deleteByProcessInstanceId(pid);
            processInstanceLogRepository.deleteByProcessInstanceId(pid);
            processInstanceInfoRepository.deleteByProcessInstanceId(pid);
        });

    }
}
