package cn.sparrow.common.controller;

import java.util.List;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import cn.sparrow.model.permission.ModelAttribute;
import cn.sparrow.model.permission.ModelAttributePK;
import cn.sparrow.model.permission.ModelAttributePermission;
import cn.sparrow.permission.repository.ModelAttributeRepository;
import cn.sparrow.permission.service.ModelAttributePermissionService;

@RestController
public class ModelAttributeController {

	@Autowired ModelAttributeRepository modelAttributeRepository;
	@Autowired ModelAttributePermissionService modelAttributePermissionService;
	
	@GetMapping("/modelAttributes")
	public Page<?> pages(@Nullable Pageable pageable) {
		return modelAttributeRepository.findAll(pageable);
	}
	
	@PostMapping("/modelAttributes/batch")
	public void add(@NotNull @RequestBody final List<ModelAttribute> modelAttributes) {
		modelAttributeRepository.saveAll(modelAttributes);
	}

	@PatchMapping("/modelAttributes/batch")
	public void update(@NotNull @RequestBody final List<ModelAttribute> modelAttributes) {
		modelAttributeRepository.saveAll(modelAttributes);
	}

	@DeleteMapping("/modelAttributes/batch")
	public void delete(@NotNull @RequestBody final List<ModelAttributePK> ids) {
		modelAttributeRepository.deleteByIdIn(ids);
	}

	@PostMapping("/modelAttributes/permissions")
	public void addPermission(@NotNull @RequestBody final ModelAttributePermission modelAttributePermission) {
		modelAttributePermissionService.addPermissions(modelAttributePermission);
	}

	@DeleteMapping("/modelAttributes/permissions")
	public void delPermission(@NotNull @RequestBody final ModelAttributePermission modelAttributePermission) {
		modelAttributePermissionService.delPermissions(modelAttributePermission);
	}

	
}