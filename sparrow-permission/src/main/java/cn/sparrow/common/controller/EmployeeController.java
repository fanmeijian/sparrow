package cn.sparrow.common.controller;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sun.istack.NotNull;

import cn.sparrow.model.common.SparrowTree;
import cn.sparrow.model.organization.Employee;
import cn.sparrow.model.organization.EmployeeOrganizationLevel;
import cn.sparrow.model.organization.EmployeeOrganizationLevelPK;
import cn.sparrow.model.organization.EmployeeOrganizationRole;
import cn.sparrow.model.organization.EmployeeOrganizationRolePK;
import cn.sparrow.model.organization.EmployeeRelation;
import cn.sparrow.model.organization.EmployeeRelationPK;
import cn.sparrow.model.organization.Role;
import cn.sparrow.organization.service.EmployeeService;
import cn.sparrow.organization.service.OrganizationService;

/**
 * 
 * @author fansword
 *
 */

@RestController
public class EmployeeController {

	@Autowired
	OrganizationService organizationService;
	@Autowired
	EmployeeService employeeService;
	
	@PutMapping("/employees/setMasterOrg")
	public void updateOrganization(@RequestParam("employeeId") String employeeId, @RequestParam("organizationId") String organizationId) {
		employeeService.saveMasterOrganization(employeeId, organizationId);
	}
	
	@GetMapping("/employees/getChildren")
	public List<EmployeeRelation> getChildren(@NotNull @RequestParam("parentId") String parentId){
		return employeeService.getChildren(parentId);
	}
	
	@GetMapping("/employees/getParents")
	public List<EmployeeRelation> getParents(@NotNull @RequestParam("employeeId") String employeeId){
		return employeeService.getParents(employeeId);
	}
	
	@GetMapping("/employees/getLevels")
	public List<EmployeeOrganizationLevel> getLevels(@NotNull @RequestParam("employeeId") String employeeId){
		return employeeService.getLevels(employeeId);
	}
	
	@GetMapping("/employees/getRoles")
	public List<EmployeeOrganizationRole> getRoles(@NotNull @RequestParam("employeeId") String employeeId){
		return employeeService.getRoles(employeeId);
	}

	@GetMapping("/employees/getModelName")
	public String getModelName() {
		return "{\"modelName\":\"" + Role.class.getName() + "\"}";
	}

	@GetMapping("/employees/getTreeByParentId")
	public SparrowTree<Employee, String> tree(@Nullable @RequestParam("parentId") String parentId) {
		return employeeService.getTree(parentId == null || parentId.isBlank() ? null : parentId);
	}

	@PostMapping("/employees/batch")
	public void add(@NotNull @RequestBody List<Employee> employees) {
	}

	@PatchMapping("/employees/batch")
	public void update(@NotNull @RequestBody List<Employee> employees) {

	}

	@DeleteMapping("/employees/batch")
	public void delete(@NotNull @RequestBody String[] ids) {
		employeeService.delBatch(ids);
	}

	@PostMapping("/employees/roles/batch")
	public void addRoles(@NotNull @RequestBody Set<EmployeeOrganizationRole> ids) {
		employeeService.addRoles(ids);
	}

	@DeleteMapping("/employees/roles/batch")
	public void delRoles(@NotNull @RequestBody Set<EmployeeOrganizationRolePK> ids) {
		employeeService.delRoles(ids);
	}

	@PostMapping("/employees/levels/batch")
	public void addLevels(@NotNull @RequestBody List<EmployeeOrganizationLevel> ids) {
		employeeService.addLevels(ids);
	}

	@DeleteMapping("/employees/levels/batch")
	public void delLevels(@NotNull @RequestBody Set<EmployeeOrganizationLevelPK> ids) {
		employeeService.delLevels(ids);
	}

	@PostMapping("/employees/relations/batch")
	public void addRelations(@NotNull @RequestBody Set<EmployeeRelation> ids) {
		employeeService.addRelations(ids);
	}

	@DeleteMapping("/employees/relations/batch")
	public void delRelations(@NotNull @RequestBody Set<EmployeeRelationPK> ids) {
		employeeService.delRelations(ids);
	}

}
