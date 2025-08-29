package cn.sparrowmini.bpm.server.common;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table
public class SysroleProcess implements Serializable {

    @EmbeddedId
    private SysroleProcessId id;

    @Embeddable
    public static class SysroleProcessId implements Serializable{
        private String sysrole;
        private String containerId;
        private String processId;

        public SysroleProcessId() {
        }

        public String getSysrole() {
            return sysrole;
        }

        public void setSysrole(String sysrole) {
            this.sysrole = sysrole;
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
