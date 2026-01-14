package cn.sparrowmini.bpm.api.service;
//

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.kie.kogito.jobs.JobsService;
import org.kie.kogito.jobs.service.executor.JobExecutor;
import org.kie.kogito.usertask.UserTaskInstance;

import java.util.Map;

@ApplicationScoped
@Path("/null")
public class MainController {
    //    @Inject
//    ReactiveJobRepository jobsService; // 注入 Job 仓库组件
    @Inject
    Instance<JobExecutor> executors;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response handleKogitoCallback(Map<String, Object> payload) {
        String cid = (String) payload.get("correlationId");
        System.out.println("回调数据" + payload);
        executors.stream().forEach(System.out::println);
//        UserTaskInstance
//        JobsService
        return Response.ok().build();
    }
}
