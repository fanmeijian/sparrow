//package cn.sparrowmini.bpm.server.config;
//
//import org.drools.compiler.kproject.models.KieBaseModelImpl;
//import org.jbpm.services.api.DeploymentService;
//import org.jbpm.services.api.ProcessService;
//import org.jbpm.services.api.RuntimeDataService;
//import org.kie.api.KieBase;
//import org.kie.api.KieServices;
//import org.kie.api.event.kiebase.*;
//import org.kie.api.runtime.Environment;
//import org.kie.api.runtime.KieRuntimeFactory;
//import org.kie.api.runtime.KieSession;
//import org.kie.api.runtime.manager.RuntimeEngine;
//import org.kie.api.runtime.manager.RuntimeManager;
//import org.kie.internal.builder.InternalKieBuilder;
//import org.kie.internal.runtime.manager.RuntimeManagerRegistry;
//import org.kie.server.api.model.KieServerConfigItem;
//import org.kie.server.client.KieServicesFactory;
//import org.kie.server.services.api.KieServer;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.PostConstruct;
//import javax.inject.Inject;
//
//@Component
//public class KieBaseListener extends DefaultKieBaseEventListener implements KieBaseEventListener {
//
//    @PostConstruct
//    public void init() {
//
//    }
//
//    @Override
//    public void beforeKiePackageAdded(BeforeKiePackageAddedEvent event) {
//
//    }
//
//    @Override
//    public void afterKiePackageAdded(AfterKiePackageAddedEvent event) {
//
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
//        System.out.println("beforeKieBaseLocked");
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
//
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
