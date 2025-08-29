package cn.sparrowmini.bpm.server.service;

import cn.sparrowmini.bpm.server.common.PageImpl;
import cn.sparrowmini.bpm.server.common.PublishedProcess;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.appformer.maven.support.AFReleaseId;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequestMapping("/jbpm-ext")
@Tag(name = "jbpm-ext",description = "jbpm扩展服务")
public interface DeploymentExtService {

    @PostMapping(value = "/kjars/deploy",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
//    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    @Operation(summary = "发布kjar", operationId = "deployKjar")
    public AFReleaseId deployKjar(@RequestParam MultipartFile[] file);

    @PostMapping(value = "/kjars/deploy-single",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    @Operation(summary = "发布kjar")
    public AFReleaseId deploy(@RequestParam MultipartFile file);


    @PostMapping(value = "/published-process")
    @ResponseBody
    @Operation(summary = "发布流程")
    public void publishProcess(@RequestBody PublishedProcess publishedProcess);

    @PostMapping(value = "/published-process/un-publish")
    @ResponseBody
    @Operation(summary = "发布流程")
    public void unPublishProcess(@RequestBody List<PublishedProcess.PublishedProcessId> publishedProcessIds);

    @GetMapping(value = "/published-process")
    @ResponseBody
    @Operation(summary = "发布流程")
    public PageImpl<PublishedProcess> getPublishedProcess(@RequestBody PublishedProcess publishedProcess);



}
