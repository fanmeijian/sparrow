package cn.sparrowmini.bpm.server.common;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Setter
@Getter
@Table
@Entity
public class VariableArchive {

    @EmbeddedId
    private VariableArchiveId id;

    private LocalDateTime completedTime;

    @Lob
    @Convert(converter = JsonMapConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<String, Object> variableJson; // 保存变量的 JSON 字符串

    @Data
    @Embeddable
    public static class VariableArchiveId implements Serializable {
        private Long processInstanceId;
        private String processId;
        private String deploymentId;

        public VariableArchiveId() {
        }

        public VariableArchiveId(Long processInstanceId, String processId, String deploymentId) {
            this.processInstanceId = processInstanceId;
            this.processId = processId;
            this.deploymentId = deploymentId;
        }

    }

}
