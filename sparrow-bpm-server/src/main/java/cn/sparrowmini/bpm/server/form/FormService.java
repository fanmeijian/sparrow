package cn.sparrowmini.bpm.server.form;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RequestMapping("/forms")
@Tag(name = "form", description = "流程表单服务")
public interface FormService {
    @GetMapping(value = "")
    @ResponseBody
    @Operation(summary = "所有表单")
    public Page<FormSchemaView> getFormSchemaList(Pageable pageable);

    @GetMapping(value = "/{id}")
    @ResponseBody
    @Operation(summary = "所有表单")
    public FormSchemaInfo getFormSchema(@PathVariable String id);


    @PostMapping(value = "")
    @ResponseBody
    @Operation(summary = "保存表单")
    public void saveFormSchema(@RequestBody FormSchema formSchema);

    @PostMapping(value = "/process-forms")
    @ResponseBody
    @Operation(summary = "关联流程")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void saveProcessForm(@RequestBody List<ProcessForm.ProcessFormId> processFormId);

    @PostMapping(value = "/task-forms")
    @ResponseBody
    @Operation(summary = "关联任务")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void saveTaskForm(@RequestBody List<TaskForm.TaskFormId> taskFormId);

    @PostMapping(value = "/process-forms/remove")
    @ResponseBody
    @Operation(summary = "移除关联流程")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void removeProcessForm(@RequestBody List<ProcessForm.ProcessFormId> processFormId);

    @PostMapping(value = "/task-forms/remove")
    @ResponseBody
    @Operation(summary = "移除关联任务")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void removeTaskForm(@RequestBody List<ProcessForm.ProcessFormId> processFormId);

}
