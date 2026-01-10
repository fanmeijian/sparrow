package cn.sparrowmini.bpm.api;

import cn.sparrowmini.bpm.api.model.TaskInstance;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;


import java.util.Map;
import java.util.Set;

public interface TaskInstanceService {

    /**
     * 获取任务列表
     * @param pageable
     * @param filter
     * @param username
     * @return
     */
    Page<TaskInstance> findTaskInstances(PageRequest pageable, String filter, String username);

    /**
     * 更新任务输出变量
     * @param taskInstanceId
     * @param variables
     */
    void updateTaskOutput(String taskInstanceId, Map<String, Object> variables);

    /**
     * 增加执行人
     * @param taskInstanceId
     * @param potentialUsers
     */
    void addPotentialUsers(String taskInstanceId, Set<String> potentialUsers);


    /**
     * 移除执行人
     * @param taskInstanceId
     * @param potentialUsers
     */
    void removePotentialUsers(String taskInstanceId, Set<String> potentialUsers);

    /**
     * 转发流程
     * @param taskInstanceId
     * @param users
     */
    void forward(String taskInstanceId, Set<String> users);

    /**
     * 跳过任务
     * @param taskInstanceId
     */
    void skip(String taskInstanceId);

    /**
     * 获取重分配用户
     * @param taskInstanceId
     */
    void getReAssignedUsers(String taskInstanceId);


    /**
     * 设置重分配任务
     * @param taskInstanceId
     * @param users
     */
    void setReAssignedUsers(String taskInstanceId, Set<String> users);

    /**
     * 委托代理任务
     * @param taskInstanceId
     */
    void delegate(String taskInstanceId, Set<String> users);

    /**
     * 跳转任务
     * @param taskInstanceId
     * @param taskId
     */
    void jump(String taskInstanceId, String taskId);
}
