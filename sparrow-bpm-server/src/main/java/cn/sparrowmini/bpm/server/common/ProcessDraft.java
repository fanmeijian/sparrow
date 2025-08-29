package cn.sparrowmini.bpm.server.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.*;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Map;

@Entity
@Table
public class ProcessDraft extends BaseOpLog implements Serializable {
    @Id
    @GenericGenerator(name = "id-generator", strategy = "uuid")
    @GeneratedValue(generator = "id-generator")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String id;

    private Long processInstanceId;

    private String deploymentId;
    private String processId;


    @Convert(converter = JsonMapConverter.class)
    @Column(columnDefinition = "TEXT") // or VARCHAR(n), or CLOB if needed
    private Map<String, Object> processData;


    public ProcessDraft(String deploymentId, String processId, Map<String, Object> body) {
        this.deploymentId=deploymentId;
        this.processId=processId;
        this.processData=body;
    }

    public ProcessDraft() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(Long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public Map<String, Object> getProcessData() {
        return processData;
    }

    public void setProcessData(Map<String, Object> processData) {
        this.processData = processData;
    }

}
