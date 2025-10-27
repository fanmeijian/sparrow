package cn.sparrowmini.bpm.server.common;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class SparrowTaskInstance implements Serializable {
    private Long id;
    private String title;
    private String name;
    private String taskName;
    private String deploymentId;
    private String processId;
    private Long processInstanceId;
    private String processName;
    private String status;
    private List<String> potentialOwners;
    private String actualOwner;
    private Date createdOn;
    private Date activationTime;
    private Map<String,Object> inputData;
    private Map<String,Object> outputData;

}
