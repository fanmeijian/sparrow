//package cn.sparrowmini.bpm.server.config;
//
//import cn.sparrowmini.bpm.server.util.MvelUtils;
//import org.drools.core.impl.KnowledgeBaseImpl;
//import org.kie.api.event.kiebase.*;
//import org.kie.api.event.process.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//@Component
//public class MyProcessEventListener implements ProcessEventListener {
//
//    @Autowired
//    private MvelUtils mvelUtils;
//
//
//    @Override
//    public void beforeProcessStarted(ProcessStartedEvent event) {
//        event.getKieRuntime().setGlobal("mvelUtils", mvelUtils);
//
//    }
//
//    @Override
//    public void afterProcessStarted(ProcessStartedEvent event) {
//
//    }
//
//    @Override
//    public void beforeProcessCompleted(ProcessCompletedEvent event) {
//
//    }
//
//    @Override
//    public void afterProcessCompleted(ProcessCompletedEvent event) {
//
//    }
//
//    @Override
//    public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
//
//    }
//
//    @Override
//    public void afterNodeTriggered(ProcessNodeTriggeredEvent event) {
//
//    }
//
//    @Override
//    public void beforeNodeLeft(ProcessNodeLeftEvent event) {
//
//    }
//
//    @Override
//    public void afterNodeLeft(ProcessNodeLeftEvent event) {
//
//    }
//
//    @Override
//    public void beforeVariableChanged(ProcessVariableChangedEvent event) {
//
//    }
//
//    @Override
//    public void afterVariableChanged(ProcessVariableChangedEvent event) {
//
//    }
//}
