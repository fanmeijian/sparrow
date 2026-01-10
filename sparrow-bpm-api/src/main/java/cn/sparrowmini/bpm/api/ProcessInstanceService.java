package cn.sparrowmini.bpm.api;

import cn.sparrowmini.bpm.api.model.ProcessInstance;
import cn.sparrowmini.bpm.api.model.ProcessInstanceVdo;
import cn.sparrowmini.bpm.api.model.ProcessInstanceVlo;
import cn.sparrowmini.bpm.api.model.VariableInstance;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;


import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ProcessInstanceService {

    /**
     * 获取流程实例列表
     * @param pageable
     * @param filters
     * @param username
     * @return
     */
    Page<ProcessInstanceVlo> getProcessInstancesByUser(PageRequest pageable, String filters, String username);

    /**
     * 获取流程实例详情
     * @param processInstanceId
     * @return
     */
    ProcessInstanceVdo getProcessInstance(String processInstanceId);

    /**
     * 完成流程实例
     * @param processInstanceId
     * @param variables
     */
    void completeProcessInstance(String processInstanceId, Map<String, Object> variables);


    /**
     * 领用流程实例
     * @param processInstanceId
     */
    void claimProcessInstance(String processInstanceId);

    /**
     * 释放流程实例
     * @param processInstanceId
     */
    void releaseProcessInstance(String processInstanceId);

    /**
     * 获取流程变量
     * @param processInstanceId
     * @param variables
     * @return
     */
    List<VariableInstance> getProcessVariables(String processInstanceId, Set<String> variables);


    /**
     * 删除流程变量
     * @param processInstanceId
     * @param variables
     */
    void removeProcessVariables(String processInstanceId, Set<String> variables);

    /**
     * 更新流程变量
     * @param processInstanceId
     * @param variables
     */
    void updateProcessVariables(String processInstanceId, Map<String, Object> variables);


}
