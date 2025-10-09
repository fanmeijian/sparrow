package cn.sparrowmini.bpm.server.dto;

import lombok.Value;
import org.kie.api.task.model.Status;

import java.io.Serializable;
import java.util.Date;

/**
 * DTO for {@link org.jbpm.services.task.impl.model.TaskDataImpl}
 */
@Value
public class TaskDataImplDto implements Serializable {
    Status status;
    Status previousStatus;
    Date createdOn;
    Date activationTime;
    Date expirationTime;
    boolean skipable;
    long workItemId;
    long processInstanceId;
    long parentId;
    String processId;
    String deploymentId;
    long processSessionId;
}