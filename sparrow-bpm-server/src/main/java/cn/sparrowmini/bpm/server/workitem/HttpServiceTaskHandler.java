package cn.sparrowmini.bpm.server.workitem;
import cn.sparrowmini.bpm.server.process.GlobalVariableMap;
import cn.sparrowmini.bpm.server.process.VariableArchiveRepository;
import cn.sparrowmini.bpm.server.repository.VariableMapRepository;
import lombok.extern.slf4j.Slf4j;
import org.drools.core.common.InternalKnowledgeRuntime;
import org.drools.core.process.instance.impl.DefaultWorkItemManager;
import org.drools.persistence.jpa.processinstance.JPAWorkItemManager;
import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.services.api.RuntimeDataService;
import org.kie.api.runtime.KieRuntime;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Map;

@Slf4j
@Component("HttpServiceTask")
public class HttpServiceTaskHandler extends AbstractLogOrThrowWorkItemHandler {

    @Autowired
    private RuntimeDataService runtimeDataService;

    @Autowired
    private VariableMapRepository variableMapRepository;

    private final WebClientService webClientService;

    public HttpServiceTaskHandler(WebClientService webClientService) {
        this.webClientService = webClientService;
    }

    @Override
    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        try {

            String containerId = runtimeDataService.getProcessInstanceById(workItem.getProcessInstanceId()).getDeploymentId();

            String method = (String) workItem.getParameter("Method");
            String url = (String) workItem.getParameter("Url");
            Object body = workItem.getParameter("Body");
            url = GlobalVariableMap.resolve(url, containerId);
            log.info("url:{} body {}",url, body);
            Object result;
            switch (method.toUpperCase()) {
                case "GET":
                    result = webClientService.get(url, String.class).block();
                    break;
                case "POST":
                    result = webClientService.post(url, body, String.class).block();
                    break;
                case "PATCH":
                    result = webClientService.patch(url, body, String.class).block();
                    break;
                case "PUT":
                    result = webClientService.put(url, body, String.class).block();
                    break;
                case "DELETE":
                    result = webClientService.delete(url, String.class).block();
                    break;
                default:
                    throw new IllegalArgumentException("不支持的方法: " + method);
            }

            manager.completeWorkItem(workItem.getId(), Map.of("Response", result==null?"": result));
        } catch (Exception e) {
            handleException(e);
        }
    }

    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {

    }
}
