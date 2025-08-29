//package cn.sparrowmini.bpm.server.listener;
//
//import cn.sparrowmini.bpm.server.process.GlobalVariableMap;
//import org.jbpm.process.instance.impl.ProcessInstanceImpl;
//import org.jbpm.ruleflow.instance.RuleFlowProcessInstance;
//import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
//import org.kie.api.event.process.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.util.Map;
//
//@Component
//public class ProcessEvent1111 extends DefaultProcessEventListener {
//    @Autowired
//    private GlobalVariableMap globalVariableMap;
//    @Override
//    public void beforeProcessStarted(ProcessStartedEvent event) {
//        globalVariableMap.getAll().forEach((k, v) -> {
//            ((WorkflowProcessInstanceImpl)event.getProcessInstance()).setVariable(k, v);
//        });
//
//    }
//
//}
