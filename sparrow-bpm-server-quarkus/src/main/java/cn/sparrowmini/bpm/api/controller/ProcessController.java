//package cn.sparrowmini.bpm.api.controller;
//
//import cn.sparrowmini.bpm.api.ProcessService;
//import cn.sparrowmini.bpm.api.model.ProcessId;
//import jakarta.enterprise.context.ApplicationScoped;
//import jakarta.inject.Inject;
//import jakarta.ws.rs.POST;
//import jakarta.ws.rs.Path;
//import jakarta.ws.rs.Produces;
//import jakarta.ws.rs.core.MediaType;
//
//import java.util.Map;
//
//@ApplicationScoped // 声明为 CDI Bean
//@Path("/api/processes") // 这里的路径会和接口中的路径拼接
//public class ProcessController {
//
//    @Inject
//    ProcessService processService;
//
//    @POST
//    @Path("/start/{processId}")
//    @Produces(MediaType.APPLICATION_JSON)
//    public String startProcess(String processId, Map<String, Object> variables) {
//        // 这里封装你的 Kogito 方法
//        processService.startProcess(new ProcessId(),null);
//        return "Process " + processId + " started" + this.processService;
//    }
//}
