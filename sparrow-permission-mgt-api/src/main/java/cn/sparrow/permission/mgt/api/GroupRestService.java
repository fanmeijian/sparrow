package cn.sparrow.permission.mgt.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.sparrow.permission.constant.GroupTypeEnum;
import cn.sparrow.permission.model.group.Group;
import cn.sparrow.permission.model.resource.SparrowTree;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;

@Tag(name = "群组服务")
@RequestMapping("/groups")
public interface GroupRestService {
	@Operation(summary = "群组树")
	@GetMapping("/tree")
	@ResponseBody
	public SparrowTree<Group, String> getTree(String parentId);

	@Operation(summary = "群组列表")
	@GetMapping("")
	@ResponseBody
	public Page<Group> all(@Nullable Pageable pageable, @Nullable Group group);
	
	@Operation(summary = "组成员列表")
	@GetMapping("/{groupId}/members")
	@ResponseBody
	public Page<?> getMembers(@PathVariable("groupId") String groupId, @NotNull GroupTypeEnum type , Pageable pageable);


}