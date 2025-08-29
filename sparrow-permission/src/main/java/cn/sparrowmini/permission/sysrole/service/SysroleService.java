package cn.sparrowmini.permission.sysrole.service;

import cn.sparrowmini.common.service.SimpleJpaFilter;
import cn.sparrowmini.permission.sysrole.model.Sysrole;
import cn.sparrowmini.permission.sysrole.model.UserSysrole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "sysrole", description = "角色服务")
@RequestMapping("/sysroles")
public interface SysroleService {

	@Operation(summary = "角色列表", operationId = "sysroles")
	@GetMapping("/filter")
	@ResponseBody
    Page<Sysrole> all(@Nullable @ParameterObject Pageable pageable,String filter);

	@Operation(summary = "新增角色", operationId = "newSysrole")
	@PostMapping("")
	@ResponseBody
    Sysrole create(@RequestBody Sysrole sysrole);

	@Operation(summary = "角色详情", operationId = "sysrole")
	@GetMapping("/{sysroleId}")
	@ResponseBody
    Sysrole get(@PathVariable("sysroleId") String sysroleId);

	@Operation(summary = "更新角色", operationId = "updateSysrole")
	@PatchMapping("/{sysroleId}")
	@ResponseBody
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(schema = @Schema(implementation = Sysrole.class)))
    Sysrole update(@PathVariable("sysroleId") String sysroleId, @RequestBody Map<String, Object> map);

	@Operation(summary = "删除角色", operationId = "deleteSysroles")
	@PostMapping("/delete")
	@ResponseBody
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
    void delete(@RequestBody List<String> ids);

	@Operation(summary = "授权用户", operationId = "addSysroleUsers")
	@PostMapping("/{sysroleId}/users")
	@ResponseBody
    void addPermissions(@PathVariable("sysroleId") String sysroleId, @RequestBody List<String> usernames);

	@Operation(summary = "取消用户授权", operationId = "removeSysroleUsers")
	@PostMapping("/{sysroleId}/users/remove")
	@ResponseBody
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
    void removePermissions(@PathVariable("sysroleId") String sysroleId, @RequestBody List<String> usernames);

	@Operation(summary = "角色用户列表", operationId = "sysroleUsers")
	@GetMapping("/{sysroleId}/users")
	@ResponseBody
    Page<UserSysrole> getUsers(@PathVariable String sysroleId, @Nullable @ParameterObject Pageable pageable);

}
