package cn.sparrowmini.bpm.server.dto;

import org.kie.api.task.model.Status;

import java.util.Date;

/**
 * Projection for {@link org.jbpm.services.task.impl.model.TaskDataImpl}
 */
public interface TaskDataImplInfo {
    Long getId();
    String getName();
    String getProcessName();

    Status getStatus();

    Date getCreatedOn();

    Date getActivationTime();

    Date getExpirationTime();

    Boolean skipable();

    Long getWorkItemId();

    Long getProcessInstanceId();

    Long getParentId();

    String getProcessId();

    String getDeploymentId();

    Long getProcessSessionId();
}