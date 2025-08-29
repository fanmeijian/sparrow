package cn.sparrowmini.bpm.server.process;



import org.jbpm.services.api.model.NodeInstanceDesc;
import org.jbpm.services.api.model.ProcessInstanceDesc;

import java.util.Collection;
import java.util.List;

public class ProcessInstanceInfo {
    private ProcessInstanceDesc processInstance;
    private List<NodeInstanceDesc> allNodes;

    public ProcessInstanceInfo(ProcessInstanceDesc processInstanceDesc, List<NodeInstanceDesc> allNodes) {
        this.processInstance=processInstanceDesc;
        this.allNodes = allNodes;
    }

    public ProcessInstanceInfo(){

    }

    public ProcessInstanceDesc getProcessInstance() {
        return processInstance;
    }

    public void setProcessInstance(ProcessInstanceDesc processInstance) {
        this.processInstance = processInstance;
    }

    public List<NodeInstanceDesc> getAllNodes() {
        return allNodes;
    }

    public void setAllNodes(List<NodeInstanceDesc> allNodes) {
        this.allNodes = allNodes;
    }
}
