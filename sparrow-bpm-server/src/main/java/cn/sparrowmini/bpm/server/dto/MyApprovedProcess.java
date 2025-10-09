package cn.sparrowmini.bpm.server.dto;

import lombok.Data;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;

@Data
public class MyApprovedProcess extends ProcessInstanceLog implements Serializable {
//    private ProcessInstanceLog processInstanceLog;
    private String title;

    public MyApprovedProcess(ProcessInstanceLog processInstanceLog, String title) {
        BeanUtils.copyProperties(processInstanceLog, this);
        this.title = title;
    }
}
