package cn.sparrowmini.bpm.server.common;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table
public class PublishedProcess implements Serializable {

    @EmbeddedId
    private PublishedProcessId id;

    private String processName;
    private String catalogId;
    private String stat;

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getCatalogId() {
        return catalogId;
    }

    public void setCatalogId(String catalogId) {
        this.catalogId = catalogId;
    }

    @Embeddable
    public static class PublishedProcessId implements Serializable{
        private String containerId;
        private String processId;


        public PublishedProcessId() {
        }

        public String getContainerId() {
            return containerId;
        }

        public void setContainerId(String containerId) {
            this.containerId = containerId;
        }

        public String getProcessId() {
            return processId;
        }

        public void setProcessId(String processId) {
            this.processId = processId;
        }
    }
}
