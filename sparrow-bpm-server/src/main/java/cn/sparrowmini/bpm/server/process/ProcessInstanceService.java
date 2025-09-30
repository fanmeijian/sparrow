package cn.sparrowmini.bpm.server.process;



import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jbpm.process.audit.NodeInstanceLog;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RequestMapping("/process-instances")
@Tag(name = "process-instance", description = "流程表单服务")
public interface ProcessInstanceService {

    @GetMapping(value = "")
    @ResponseBody
    @Operation(summary = "保存流程设计")
    public Page<ProcessInstanceLog> allProcessInstance(Pageable pageable);

    @GetMapping(value = "/my")
    @ResponseBody
    @Operation(summary = "我发起的流程", operationId = "my-process-instances")
    public Page<ProcessInstanceLog> MyProcessInstances(Pageable pageable);


    @GetMapping(value = "/{processInstanceId}/node-instances")
    @ResponseBody
    @Operation(summary = "保存流程设计")
    public Page<NodeInstanceLog> getNodeInstanceByProcessInstanceId(@PathVariable Long processInstanceId,Pageable pageable);

    @GetMapping(value = "/{processInstanceId}")
    @ResponseBody
    @Operation(summary = "保存流程设计")
    public ProcessInstanceLog getProcessInstance(@PathVariable Long processInstanceId);

    @PutMapping(value = "/node-instance/trigger")
    @ResponseBody
    @Operation(summary = "保存流程设计")
    public void triggerNode(String containerId, Long processInstanceId, Long nodeInstanceId);

    @DeleteMapping(value = "delete")
    @ResponseBody
    @Operation(summary = "删除流程")
    public void deleteProcessInstance(@RequestParam Set<Long> processInstanceIds);

    @PostMapping
    @ResponseBody
    public void reopenProcessInstance(Long processInstanceId,Long nodeInstanceId);
}
