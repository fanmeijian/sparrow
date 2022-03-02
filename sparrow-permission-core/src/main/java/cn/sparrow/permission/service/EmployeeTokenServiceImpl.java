package cn.sparrow.permission.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.sparrow.permission.model.organization.Employee;
import cn.sparrow.permission.model.organization.EmployeeUser;
import cn.sparrow.permission.model.organization.OrganizationPositionLevelPK;
import cn.sparrow.permission.model.organization.OrganizationRolePK;
import cn.sparrow.permission.repository.UserSysroleRepository;
import cn.sparrow.permission.repository.group.GroupEmployeeRepository;
import cn.sparrow.permission.repository.organization.EmployeeOrganizationLevelRepository;
import cn.sparrow.permission.repository.organization.EmployeeOrganizationRoleRepository;
import cn.sparrow.permission.repository.organization.EmployeeRepository;
import cn.sparrow.permission.repository.organization.EmployeeTokenRepository;
import cn.sparrow.permission.repository.organization.EmployeeUserRepository;

@Service
public class EmployeeTokenServiceImpl implements EmployeeTokenService {

	@Autowired
	EmployeeTokenRepository employeeTokenRepository;
	@Autowired
	EmployeeRepository employeeRepository;
	@Autowired
	EmployeeUserRepository employeeUserRepository;
	@Autowired
	UserSysroleRepository userSysroleRepository;
	@Autowired
	EmployeeOrganizationRoleRepository employeeOrganizationRoleRepository;
	
	@Autowired
	EmployeeOrganizationLevelRepository employeeOrganizationLevelRepository;
	
	@Autowired
	GroupEmployeeRepository groupEmployeeRepository;

	@Override
	// build it from data base, use to get the latest token
	public EmployeeToken buildEmployeeToken(String username) {
		EmployeeUser employeeUser = employeeUserRepository.findOneByIdUsername(username);
		if (employeeUser == null) {
			return null;
		}
		String employeeId = employeeUser.getId().getEmployeeId();
		Employee employee = employeeRepository.findById(employeeId).get();

		List<String> usernames = new ArrayList<String>();
		List<String> sysroles = new ArrayList<String>();
		List<String> organizations = new ArrayList<String>();
		List<OrganizationRolePK> rolePKs = new ArrayList<OrganizationRolePK>();
		List<OrganizationPositionLevelPK> positionLevelPKs = new ArrayList<OrganizationPositionLevelPK>();
		List<String> groups = new ArrayList<String>();

		// 员工拥有的登录账户
//		employee.getEmployeeUsers().forEach(eu -> {
//			usernames.add(eu.getId().getUsername());
//			userSysroleRepository.findByIdUsername(username).forEach(us -> {
//				sysroles.add(us.getId().getSysroleId());
//			});
//		});
		employeeUserRepository.findByIdEmployeeId(employeeId).forEach(eu -> {
			usernames.add(eu.getId().getUsername());
			userSysroleRepository.findByIdUsername(username).forEach(us -> {
				sysroles.add(us.getId().getSysroleId());
			});
		});
		
		// 员工所在组织列表
		organizations.add(employee.getOrganizationId());

		// 员工担任的岗位列表
		employeeOrganizationRoleRepository.findByIdEmployeeId(employeeId).forEach(f -> {
			rolePKs.add(f.getId().getOrganizationRoleId());
			organizations.add(f.getId().getOrganizationRoleId().getOrganizationId());
		});

		// 员工的级别
		employeeOrganizationLevelRepository.findByIdEmployeeId(employeeId).forEach(f -> {
			positionLevelPKs.add(f.getId().getOrganizationLevelId());
//			organizations.add(f.getId().getOrganizationLevelId().getOrganizationId());
		});

		// 员工所在的组
		groupEmployeeRepository.findByIdEmployeeId(employeeId).forEach(f -> {
			groups.add(f.getId().getGroupId());
		});

		EmployeeToken employeeToken = new EmployeeToken();
		employeeToken.setEmployeeId(employeeId);
		employeeToken.setUsernames(usernames);
		employeeToken.setSysroleIds(sysroles);
		employeeToken.setGroupIds(groups);
		employeeToken.setOrganizationIds(organizations);
		employeeToken.setRoleIds(rolePKs);
		employeeToken.setPositionLevelIds(positionLevelPKs);

		return employeeToken;

	}

	/**
	 * get it from data base, the token store in database when user login.
	 * 
	 * @param username
	 * @return
	 */

	@Override
	public EmployeeToken getEmployeeToken(String username) {
		EmployeeUser employeeUser = employeeUserRepository.findOneByIdUsername(username);
		if (employeeUser != null) {
			EmployeeToken employeeToken = employeeTokenRepository
					.findOneByEmployeeId(employeeUser.getId().getEmployeeId());
			if (employeeToken != null)
				return employeeToken;
		}
		return buildEmployeeToken(username);
	}
}
