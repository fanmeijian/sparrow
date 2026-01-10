package cn.sparrowmini.bpm.api;

import cn.sparrowmini.bpm.api.model.*;
import cn.sparrowmini.bpm.api.model.Process;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;


import java.util.List;
import java.util.Map;

/**
 * 用来管理流程定义，发布和授权的服务
 */
public interface ProcessService {
    /**
     * 获取用户可以使用的流程
     * @param pageable
     * @param filter
     * @param username
     * @return
     */
    Page<ProcessVlo> getProcessesByUser(PageRequest pageable, String filter, String username);

    /**
     * 流程详情
     * @param processId
     * @return
     */
    ProcessVdo getProcess(ProcessId processId);

    /**
     * 启动流程
     * @param processId
     * @param variables
     */
    void startProcess(ProcessId processId, Map<String, Object> variables);

    /**
     * 终止流程
     * @param processId
     * @param processInstanceId
     * @param reason
     */
    void stopProcess(ProcessId processId, String processInstanceId, String reason);


    /**
     * 发布流程
     * @param processes
     */
    void publishProcess(List<Process> processes);

    /**
     * 下架流程
     * @param processIds
     */
    void unpublishProcess(List<ProcessId> processIds);

    /**
     * 流程权限详情
     * @param processId
     * @return
     */
    PermissionDto getProcessPermissions(ProcessId processId);

    /**
     * 授权
     * @param processId
     * @param permission
     */
    void grantProcessPermission(ProcessId processId, PermissionDto permission);

    /**
     * 删除权限
     * @param processId
     * @param permission
     */
    void removeProcessPermission(ProcessId processId, PermissionDto permission);
}
