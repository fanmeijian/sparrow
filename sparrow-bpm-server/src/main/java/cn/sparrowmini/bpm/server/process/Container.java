package cn.sparrowmini.bpm.server.process;

import cn.sparrowmini.bpm.server.common.BaseOpLog;
import lombok.*;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Container extends BaseOpLog implements Serializable {


    private String name;
    private String remark;

    @EmbeddedId
    private ContainerId id;


    @Data
    @Embeddable
    public static class ContainerId implements Serializable {
        private String groupId;
        private String artifactId;
        private String version;

    }
}
