//package cn.sparrowmini.bpm.server.listener;
//
//import org.kie.api.event.kiebase.*;
//import org.springframework.stereotype.Component;
//
//@Component
//public class KieBaseEvent111 implements KieBaseEventListener {
//    @Override
//    public void beforeKiePackageAdded(BeforeKiePackageAddedEvent event) {
//        System.out.println("beforeKiePackageAdded");
//    }
//
//    @Override
//    public void afterKiePackageAdded(AfterKiePackageAddedEvent event) {
//        System.out.println("after KiePackageAdded");
//    }
//
//    @Override
//    public void beforeKiePackageRemoved(BeforeKiePackageRemovedEvent event) {
//
//    }
//
//    @Override
//    public void afterKiePackageRemoved(AfterKiePackageRemovedEvent event) {
//
//    }
//
//    @Override
//    public void beforeKieBaseLocked(BeforeKieBaseLockedEvent event) {
//
//    }
//
//    @Override
//    public void afterKieBaseLocked(AfterKieBaseLockedEvent event) {
//
//    }
//
//    @Override
//    public void beforeKieBaseUnlocked(BeforeKieBaseUnlockedEvent event) {
//
//    }
//
//    @Override
//    public void afterKieBaseUnlocked(AfterKieBaseUnlockedEvent event) {
//
//    }
//
//    @Override
//    public void beforeRuleAdded(BeforeRuleAddedEvent event) {
//
//    }
//
//    @Override
//    public void afterRuleAdded(AfterRuleAddedEvent event) {
//
//    }
//
//    @Override
//    public void beforeRuleRemoved(BeforeRuleRemovedEvent event) {
//
//    }
//
//    @Override
//    public void afterRuleRemoved(AfterRuleRemovedEvent event) {
//
//    }
//
//    @Override
//    public void beforeFunctionRemoved(BeforeFunctionRemovedEvent event) {
//
//    }
//
//    @Override
//    public void afterFunctionRemoved(AfterFunctionRemovedEvent event) {
//
//    }
//
//    @Override
//    public void beforeProcessAdded(BeforeProcessAddedEvent event) {
//
//    }
//
//    @Override
//    public void afterProcessAdded(AfterProcessAddedEvent event) {
//        event.getKieBase().getKieSessions().forEach(session -> {session.setGlobal("","");});
//    }
//
//    @Override
//    public void beforeProcessRemoved(BeforeProcessRemovedEvent event) {
//
//    }
//
//    @Override
//    public void afterProcessRemoved(AfterProcessRemovedEvent event) {
//
//    }
//}
