package cn.sparrowmini.permission.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import cn.sparrowmini.common.model.ErrMessage;
import cn.sparrowmini.permission.sysrole.model.Sysrole;
import cn.sparrowmini.permission.sysrole.model.UserSysrole;
import cn.sparrowmini.permission.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "user", description = "用户服务")
@RequestMapping("/users")
public interface UserService {

	List<Sysrole> getSysroles(String username);

	List<UserSysrole> getUserSysroles(String username);

	@GetMapping("/filter")
	@ResponseBody
	@Operation(summary = "用户列表", operationId = "users")
    Page<User> getAllUsers(@Nullable @ParameterObject Pageable pageable, String filter);

	@GetMapping("/synchronize")
	@ResponseBody
	@Operation(summary = "同步用户", operationId = "synchronize")
    void synchronize();

	@PostMapping("")
	@ResponseBody
	@ResponseStatus(code = HttpStatus.CREATED)
	@Operation(summary = "添加用户", operationId = "createUser")
    Map<String, List<ErrMessage>> create(@RequestBody Set<User> users);

	@PatchMapping("/{username}")
	@ResponseBody
	@Operation(summary = "更新用户", operationId = "updateUser")
    List<ErrMessage> update(@PathVariable String username, @RequestBody Map<String, Object> map);

	@GetMapping("/{username}")
	@ResponseBody
	@Operation(summary = "用户详情", operationId = "user")
    User get(@PathVariable String username);

	@PostMapping("/delete")
	@ResponseBody
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@Operation(summary = "删除用户", operationId = "deleteUser")
    void 删除用户(@RequestBody Set<String> usernames);


	@PostMapping("/{username}/enable")
	@ResponseBody
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@Operation(summary = "启用/禁用用户", operationId = "enableUser")
    void enable(@PathVariable String username, @RequestParam Boolean enabled);

	@PostMapping("/{username}/reset-password")
	@ResponseBody
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@Operation(summary = "重置用户密码", operationId = "resetPassword")
    void resetPassword(@PathVariable String username, @RequestBody String password);

	@GetMapping("/session/sysroles")
	@ResponseBody
	@Operation(summary = "当前用户的角色列表", operationId = "mySysroles")
    List<Sysrole> mySysroles();

}
