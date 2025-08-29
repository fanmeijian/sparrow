package cn.sparrowmini.bpm.server.process;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/process-design")
@Tag(name = "process-design", description = "流程设计")
public interface ProcessDesignerService {

    @PostMapping(value = "/containers")
    @ResponseBody
    @Operation(summary = "保存容器")
    public void saveContainer(@RequestBody Container container);

    @DeleteMapping(value = "/containers")
    @ResponseBody
    @Operation(summary = "删除容器")
    public void deleteContainer(Container.ContainerId containerId);

    @GetMapping(value = "/containers")
    @ResponseBody
    @Operation(summary = "容器列表")
    public Page<Container> getContainerList(Pageable pageable);


    @PostMapping(value = "")
    @ResponseBody
    @Operation(summary = "保存流程设计")
    public void saveProcessDesign(@RequestBody ProcessDesign processDesign);

    @GetMapping(value = "")
    @ResponseBody
    @Operation(summary = "流程设计列表")
    public Page<ProcessDesignDto> getProcessDesignList(Container.ContainerId containerId, Pageable pageable);

    @GetMapping(value = "/view")
    @ResponseBody
    @Operation(summary = "流程设计详情")
    public ProcessDesign getProcessDesign(ProcessDesign.ProcessDesignId id);

    @PostMapping(value = "/deploy")
    @ResponseBody
    @Operation(summary = "部署容器")
    public void deployProcessDesign(@RequestBody Container.ContainerId containerId);

    @DeleteMapping(value = "/containers/{containerId}/un-deploy")
    @ResponseBody
    @Operation(summary = "卸载容器")
    public void unDeployProcessDesign(@PathVariable String containerId);
}
