package cn.sparrow.permission.mgt.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.StringMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseStatus;

import cn.sparrow.permission.mgt.api.EmployeeService;
import cn.sparrow.permission.mgt.api.scopes.EmployeeScope;
import cn.sparrow.permission.mgt.service.repository.EmployeeOrganizationLevelRepository;
import cn.sparrow.permission.mgt.service.repository.EmployeeOrganizationRoleRepository;
import cn.sparrow.permission.mgt.service.repository.EmployeeRelationRepository;
import cn.sparrow.permission.mgt.service.repository.EmployeeRepository;
import cn.sparrow.permission.mgt.service.repository.PositionLevelRepository;
import cn.sparrow.permission.mgt.service.repository.RoleRepository;
import cn.sparrow.permission.model.organization.Employee;
import cn.sparrow.permission.model.organization.EmployeeOrganizationLevel;
import cn.sparrow.permission.model.organization.EmployeeOrganizationLevelPK;
import cn.sparrow.permission.model.organization.EmployeeOrganizationRole;
import cn.sparrow.permission.model.organization.EmployeeOrganizationRolePK;
import cn.sparrow.permission.model.organization.EmployeeRelation;
import cn.sparrow.permission.model.organization.EmployeeRelationPK;
import cn.sparrow.permission.model.organization.OrganizationPositionLevelPK;
import cn.sparrow.permission.model.organization.OrganizationRolePK;
import cn.sparrow.permission.model.resource.SparrowTree;

@Service
public class EmployeeServiceImpl extends AbstractPreserveScope implements EmployeeService, EmployeeScope {

	@Autowired
	EmployeeRelationRepository employeeRelationRepository;
	@Autowired
	EmployeeRepository employeeRepository;
	@Autowired
	EmployeeOrganizationRoleRepository employeeOrganizationRoleRepository;
	@Autowired
	EmployeeOrganizationLevelRepository employeeOrganizationLevelRepository;
	@Autowired
	RoleRepository roleRepository;
	@Autowired
	PositionLevelRepository positionLevelRepository;

	@Override
	@Transactional
	@PreAuthorize("hasAuthority('SCOPE_"+SCOPE_ADMIN_UPDATE+"') or hasRole('ROLE_"+ROLE_ADMIN+"')")
	public Employee update(String employeeId, Map<String, Object> map) {
		Employee source = employeeRepository.getById(employeeId);
		PatchUpdateHelper.merge(source, map);
		return employeeRepository.save(source);
	}

	@Override
	@Transactional
	@ResponseStatus(code = HttpStatus.CREATED)
	@PreAuthorize("hasAuthority('SCOPE_"+SCOPE_ADMIN_CREATE+"') or hasRole('ROLE_"+ROLE_ADMIN+"')")
	public Employee create(Employee employee) {
		return employeeRepository.save(employee);
	}

	@Override
	@Transactional
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@PreAuthorize("hasAuthority('SCOPE_"+SCOPE_ADMIN_PARENT_ADD+"') or hasRole('ROLE_"+ROLE_ADMIN+"')")
	public void addParent(String employeeId, List<String> parentIds) {
		parentIds.forEach(f -> {
			employeeRelationRepository.save(new EmployeeRelation(employeeId, f));
		});
	}

	@Override
	@Transactional
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@PreAuthorize("hasAuthority('SCOPE_"+SCOPE_ADMIN_PARENT_REMOVE+"') or hasRole('ROLE_"+ROLE_SUPER_ADMIN+"')")
	public void removeParent(String employeeId, List<String> parentIds) {
		parentIds.forEach(f -> {
			employeeRelationRepository.deleteById(new EmployeeRelationPK(employeeId, f));
		});
	}

	@Override
	@Transactional
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@PreAuthorize("hasAuthority('SCOPE_"+SCOPE_ADMIN_ROLE_ADD+"') or hasRole('ROLE_"+ROLE_ADMIN+"')")
	public void addRole(String employeeId, List<OrganizationRolePK> ids) {
		ids.forEach(f -> {
			employeeOrganizationRoleRepository.save(new EmployeeOrganizationRole(employeeId, f));
		});
	}

	@Override
	@Transactional
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@PreAuthorize("hasAuthority('SCOPE_"+SCOPE_ADMIN_ROLE_REMOVE+"') or hasRole('ROLE_"+ROLE_SUPER_ADMIN+"')")
	public void removeRole(String employeeId, List<OrganizationRolePK> ids) {
		ids.forEach(f -> {
			employeeOrganizationRoleRepository.deleteById(new EmployeeOrganizationRolePK(f, employeeId));
		});
	}

	@Override
	@Transactional
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@PreAuthorize("hasAuthority('SCOPE_"+SCOPE_ADMIN_LEVEL_ADD+"') or hasRole('ROLE_"+ROLE_ADMIN+"')")
	public void addLevel(String employeeId, List<OrganizationPositionLevelPK> ids) {
		ids.forEach(f -> {
			employeeOrganizationLevelRepository.save(new EmployeeOrganizationLevel(employeeId, f));
		});
	}

	@Override
	@Transactional
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@PreAuthorize("hasAuthority('SCOPE_"+SCOPE_ADMIN_LEVEL_REMOVE+"') or hasRole('ROLE_"+ROLE_SUPER_ADMIN+"')")
	public void removeLevel(String employeeId, List<OrganizationPositionLevelPK> ids) {
		ids.forEach(f -> {
			employeeOrganizationLevelRepository.deleteById(new EmployeeOrganizationLevelPK(f, employeeId));
		});
	}

	public SparrowTree<Employee, String> getTree(String parentId) {
		if (parentId == null) {
			SparrowTree<Employee, String> rootTree = new SparrowTree<Employee, String>(null);
			employeeRepository.findByIsRoot(true).forEach(f -> {
				SparrowTree<Employee, String> myTree = new SparrowTree<Employee, String>(f);
				buildTree(myTree);
				rootTree.getChildren().add(myTree);
			});

			return rootTree;
		} else {
			SparrowTree<Employee, String> myTree = new SparrowTree<Employee, String>(
					employeeRepository.findById(parentId).orElse(null));
			buildTree(myTree);
			return myTree;
		}
	}

	public void buildTree(SparrowTree<Employee, String> myTree) {
		employeeRelationRepository.findByIdParentId(myTree.getMe() == null ? null : myTree.getMe().getId())
				.forEach(f -> {
					SparrowTree<Employee, String> leaf = new SparrowTree<Employee, String>(f.getEmployee());
					// 防止死循环
					if (employeeRelationRepository
							.findById(new EmployeeRelationPK(f.getId().getParentId(), f.getId().getEmployeeId()))
							.orElse(null) == null)
						buildTree(leaf);
					myTree.getChildren().add(leaf);
				});
	}

	@Transactional
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@PreAuthorize("hasAuthority('SCOPE_"+SCOPE_ADMIN_DELETE+"') or hasRole('ROLE_"+ROLE_SUPER_ADMIN+"')")
	public void delBatch(String[] ids) {
		employeeRelationRepository.deleteByIdEmployeeIdInOrIdParentIdIn(ids, ids);
		employeeRepository.deleteByIdIn(ids);
	}

	@Override
	@PreAuthorize("hasAuthority('SCOPE_"+SCOPE_ADMIN_CHILD_LIST+"') or hasRole('ROLE_"+ROLE_ADMIN+"')")
	public List<EmployeeRelation> getChildren(String employeeId) {
		return employeeRelationRepository.findByIdParentId(employeeId);
	}

	public long getChildCount(String parentId) {
		return employeeRelationRepository.countByIdParentId(parentId);
	}

	@Override
	@PreAuthorize("hasAuthority('SCOPE_"+SCOPE_ADMIN_PARENT_LIST+"') or hasRole('ROLE_"+ROLE_ADMIN+"')")
	public List<EmployeeRelation> getParents(String employeeId) {
		return employeeRelationRepository.findByIdEmployeeId(employeeId);
	}

	@Override
	@PreAuthorize("hasAuthority('SCOPE_"+SCOPE_ADMIN_LEVEL_LIST+"') or hasRole('ROLE_"+ROLE_ADMIN+"')")
	public List<EmployeeOrganizationLevel> getLevels(String employeeId) {
		return employeeOrganizationLevelRepository.findByIdEmployeeId(employeeId);
	}

	@Override
	@PreAuthorize("hasAuthority('SCOPE_"+SCOPE_ADMIN_ROLE_LIST+"') or hasRole('ROLE_"+ROLE_ADMIN+"')")
	public List<EmployeeOrganizationRole> getRoles(String employeeId) {
		return employeeOrganizationRoleRepository.findByIdEmployeeId(employeeId);
	}

	@Override
	public SparrowTree<Employee, String> tree(String parentId) {
		return getTree(parentId);
	}

	@Override
	@Transactional
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@PreAuthorize("hasAuthority('SCOPE_"+SCOPE_ADMIN_DELETE+"') or hasRole('ROLE_"+ROLE_SUPER_ADMIN+"')")
	public void delete(String[] ids) {
		employeeRepository.deleteByIdIn(ids);
	}

	@Override
	@PreAuthorize("hasAuthority('SCOPE_"+SCOPE_ADMIN_READ+"') or hasRole('ROLE_"+ROLE_ADMIN+"')")
	public Employee get(String employeeId) {
		return employeeRepository.findById(employeeId).orElse(null);
	}

	@Override
	@PreAuthorize("hasAuthority('SCOPE_"+SCOPE_ADMIN_LIST+"') or hasRole('ROLE_"+ROLE_ADMIN+"')")
	public Page<Employee> all(Pageable pageable, Employee employee) {
		ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreCase().withStringMatcher(StringMatcher.CONTAINING);
		return employeeRepository.findAll(Example.of(employee, matcher), pageable);
	}

}
