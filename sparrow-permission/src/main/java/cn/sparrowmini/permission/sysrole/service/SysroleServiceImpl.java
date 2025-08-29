package cn.sparrowmini.permission.sysrole.service;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import cn.sparrowmini.common.constant.PreserveSysroleEnum;
import cn.sparrowmini.common.service.ScopePermission;
import cn.sparrowmini.permission.scope.bean.SysroleScope;
import cn.sparrowmini.permission.sysrole.model.Sysrole;
import cn.sparrowmini.permission.sysrole.model.UserSysrole;
import cn.sparrowmini.permission.sysrole.repository.SysroleRepository;
import cn.sparrowmini.permission.sysrole.repository.UserSysroleRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * 系统的角色管理，仅允许管理员和超级管理员处理
 *
 * @author fansword
 *
 */

@Slf4j
@Service
@RestController
public class SysroleServiceImpl implements SysroleService {
	@Autowired
    SysroleRepository sysroleRepository;

	@Autowired
	UserSysroleRepository userSysroleRepository;
//	@Autowired
//	SysroleMenuRepository sysroleMenuRepository;

	public void delMenus(String sysroleId, List<String> menuIds) {
//		sysroleMenuRepository.deleteByIdSysroleIdAndIdMenuIdIn(sysroleId, menuIds);
	}

	@Override
	@ScopePermission(name = "", scope = SysroleScope.SysroleUserScope.LIST)
	public Page<UserSysrole> getUsers(String sysroleId, Pageable pageable) {
		if(sysroleRepository.existsById(sysroleId)){
			return userSysroleRepository.findByIdSysroleId(sysroleId, pageable);
		}else{
			return userSysroleRepository.findBySysroleCode(sysroleId, pageable);
		}
	}

	@ScopePermission(scope = "admin:sysrole:menu:add", name = "添加角色菜单")
	public void addMenus(String sysroleId, List<String> menuIds) {
//		this.sysroleMenuRepository.saveAll(menuIds.stream().map(m->new SysroleMenu(m,sysroleId)).collect(Collectors.toList()));
	}

	public void init() {
		sysroleRepository.save(new Sysrole("超级管理员", PreserveSysroleEnum.SYSADMIN.name()));
		log.info("Create sysrole {}", PreserveSysroleEnum.SYSADMIN.name());

		sysroleRepository.save(new Sysrole("系统管理员", PreserveSysroleEnum.ADMIN.name()));
		log.info("Create sysrole {}", PreserveSysroleEnum.ADMIN.name());

		userSysroleRepository.save(new UserSysrole(new UserSysrole.UserSysroleId("ROOT",
				sysroleRepository.findByCode(PreserveSysroleEnum.SYSADMIN.name()).getId())));
		log.info("Grant user {} sysrole SYSADMIN", PreserveSysroleEnum.ADMIN.name());

	}

	@Override
	@Transactional
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@ScopePermission(name = "", scope = SysroleScope.SysroleUserScope.ADD)
	public void addPermissions(String sysroleId, List<String> usernames) {
		this.userSysroleRepository.saveAll(usernames.stream().map(m->new UserSysrole(sysroleId, m)).collect(Collectors.toList()));
	}

	@Override
	@Transactional
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@ScopePermission(name = "", scope = SysroleScope.SysroleUserScope.REMOVE)
	public void removePermissions(String sysroleId, List<String> usernames) {
		this.userSysroleRepository.deleteAllById(usernames.stream().map(m->new UserSysrole.UserSysroleId(m, sysroleId)).collect(Collectors.toList()));
	}

	@Override
	@Transactional
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@ScopePermission(name = "", scope = SysroleScope.DELETE)
	public void delete(List<String> ids) {
		sysroleRepository.deleteAllById(ids);
	}

	@Override
	@ScopePermission(name = "", scope = SysroleScope.LIST)
	public Page<Sysrole> all(Pageable pageable, String filter) {
		return sysroleRepository.findAll(pageable, filter);
	}

	@Override
	@ResponseStatus(code = HttpStatus.CREATED)
	@ScopePermission(name = "", scope = SysroleScope.CREATE)
	public Sysrole create(Sysrole sysrole) {
		return sysroleRepository.save(sysrole);
	}

	@Transactional
	@Override
	@ScopePermission(name = "", scope = SysroleScope.UPDATE)
	public Sysrole update(String sysroleId, Map<String, Object> map) {
		Sysrole source = sysroleRepository.getReferenceById(sysroleId);
//		PatchUpdateHelper.merge(source, map);
		return sysroleRepository.saveAndFlush(source);
	}

	@Override
	@ScopePermission(name = "", scope = SysroleScope.READ)
	public Sysrole get(String sysroleId) {
		return sysroleRepository.findById(sysroleId).get();
	}
}
