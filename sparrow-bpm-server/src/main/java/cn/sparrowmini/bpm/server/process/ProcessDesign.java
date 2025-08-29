package cn.sparrowmini.bpm.server.process;

import cn.sparrowmini.bpm.server.common.BaseOpLog;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.kie.api.definition.process.Process;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
public class ProcessDesign extends BaseOpLog {

    @EmbeddedId
    private ProcessDesignId id;

    private String processName;

    @Embedded
    private Container.ContainerId containerId;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String bpmnXml;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String svg;

    @Transient
    private List<Map<String, String>> tasks;

    public List<Map<String, String>> getTasks() {
        List<Process> process = BpmnHelper.getProcesses(this.bpmnXml);
        List<Map<String, String>> tasks = new ArrayList<>();
        BpmnHelper.parseBpmnTasks(process).get(this.id.processId).forEach(humanTaskNode -> {
            tasks.add(Map.of("taskName",humanTaskNode.getWork().getParameter("TaskName").toString(), "nodeName",humanTaskNode.getName()));
        });
        return tasks;
    }

    public ProcessDesign(ProcessDesignId id, String bpmnXml, String processName) {
        this.id = id;
        this.bpmnXml = bpmnXml;
        this.processName = processName;
    }


    @MappedSuperclass
    @Embeddable
    @Data
    @NoArgsConstructor
    public static class ProcessDesignId implements Serializable {
        private String processId;
        private String processVersion;
        private String packageName;

        public ProcessDesignId(String processId, String processVersion, String packageName) {
            this.processId = processId;
            this.processVersion = processVersion;
            this.packageName = packageName;
        }

    }
}
