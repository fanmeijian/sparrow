package cn.sparrowmini.form.service;

import cn.sparrowmini.form.model.SparrowForm;
import cn.sparrowmini.form.model.SparrowFormData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RequestMapping("/forms")
@Tag(name = "form",description = "表单服务")
public interface SparrowFormService {
	@PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@ResponseStatus(code = HttpStatus.CREATED)
	@Operation(summary = "创建数据表单", operationId = "createDataForm")
	public void saveForm(@RequestBody SparrowForm form);
	
	@PutMapping(value = "/{formId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@Operation(summary = "更新数据表单", operationId = "updateDataForm")
	public void updateForm(@PathVariable String formId, @RequestBody SparrowForm form);

	@GetMapping(value = "/{formId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@Operation(summary = "数据表单详情", operationId = "dataForm")
	public SparrowForm getForm(@PathVariable String formId);
	
	@PostMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@Operation(summary = "删除数据表单", operationId = "deleteDataForm")
	public void deleteForm(@RequestBody Set<String> ids);

	@GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@Operation(summary = "数据表单列表", operationId = "dataForms")
	public Page<SparrowForm> getForms(@ParameterObject Pageable pageable);

	@PostMapping(value = "/{formId}/datas", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@ResponseStatus(code = HttpStatus.CREATED)
	@Operation(summary = "保存表单数据", operationId = "saveFormData")
	public void saveData(@PathVariable String formId, @RequestBody String data);
	
	@PutMapping(value = "/datas/{dataId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@Operation(summary = "更新表单数据", operationId = "updateFormData")
	public void updateData(@PathVariable String dataId, @RequestBody String data);

	@GetMapping(value = "/datas/{formDataId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@Operation(summary = "表单数据详情", operationId = "formData")
	public SparrowFormData getFormData(@PathVariable String formDataId);

	@GetMapping(value = "/{formId}/datas", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@Operation(summary = "表单数据列表", operationId = "formDatas")
	public Page<SparrowFormData> getFormDatas(@PathVariable String formId,@ParameterObject Pageable pageable);
	
	@GetMapping(value = "/datas", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@Operation(summary = "所有数据列表", operationId = "allFormDatas")
	public Page<SparrowFormData> getFormsDatas(@ParameterObject Pageable pageable);
	
	@PostMapping(value = "/datas/delete", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@Operation(summary = "删除数据", operationId = "deleteFormData")
	public void deleteFormData(@RequestBody Set<String> ids);

}
