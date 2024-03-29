package cn.sparrow.permission.core.service;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import cn.sparrow.permission.core.api.EmployeeTokenService;
import cn.sparrow.permission.model.group.GroupEmployee;
import cn.sparrow.permission.model.organization.Employee;
import cn.sparrow.permission.model.organization.EmployeeOrganizationLevel;
import cn.sparrow.permission.model.organization.EmployeeOrganizationRole;
import cn.sparrow.permission.model.organization.EmployeeUser;
import cn.sparrow.permission.model.organization.OrganizationPositionLevelPK;
import cn.sparrow.permission.model.organization.OrganizationRolePK;
import cn.sparrow.permission.model.resource.UserSysrole;
import cn.sparrow.permission.model.token.EmployeeToken;
import cn.sparrow.permission.model.token.SparrowEmployeeToken;

public class EmployeeTokenServiceImpl implements EmployeeTokenService {
	private EntityManager entityManager;

	@Override
	// build it from data base, use to get the latest token
	public EmployeeToken buildEmployeeTokenWithUsername(String username) {
		List<EmployeeUser> employeeUsers = entityManager
				.createNamedQuery("EmployeeUser.findByUsername", EmployeeUser.class).setParameter("username", username)
				.getResultList();
		EmployeeUser employeeUser = employeeUsers.get(0);
		return buildEmployeeTokenWithEmployeeId(employeeUser.getId().getEmployeeId());
	}

	/**
	 * get it from data base, the token store in database when user login.
	 * 
	 * @param username
	 * @return
	 */

	@Override
	public EmployeeToken getEmployeeTokenByUsername(String username) {
		try {
			EmployeeUser employeeUser = entityManager
					.createNamedQuery("EmployeeUser.findByUsername", EmployeeUser.class)
					.setParameter("username", username).getSingleResult();
			SparrowEmployeeToken sparrowEmployeeToken = entityManager.find(SparrowEmployeeToken.class,
					employeeUser.getId().getEmployeeId());
			if (sparrowEmployeeToken != null && sparrowEmployeeToken.getEmployeeToken() != null)
				return sparrowEmployeeToken.getEmployeeToken();
			else {
				return buildEmployeeTokenWithUsername(username);
			}
		} catch (NoResultException e) {
			return null;
		}
	}

	public EmployeeTokenServiceImpl(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public EmployeeTokenServiceImpl() {
	}

	@Override
	public EmployeeToken getEmployeeTokenByEmployeeId(String employeeId) {
		SparrowEmployeeToken sparrowEmployeeToken = entityManager.find(SparrowEmployeeToken.class, employeeId);
		if (sparrowEmployeeToken != null && sparrowEmployeeToken.getEmployeeToken() != null)
			return sparrowEmployeeToken.getEmployeeToken();
		else {
			return buildEmployeeTokenWithEmployeeId(employeeId);
		}
	}

	@Override
	public EmployeeToken buildEmployeeTokenWithEmployeeId(String employeeId) {

		Employee employee = entityManager.find(Employee.class, employeeId);

		List<String> usernames = new ArrayList<String>();
		List<String> sysroles = new ArrayList<String>();
		List<String> organizations = new ArrayList<String>();
		List<OrganizationRolePK> rolePKs = new ArrayList<OrganizationRolePK>();
		List<OrganizationPositionLevelPK> positionLevelPKs = new ArrayList<OrganizationPositionLevelPK>();
		List<String> groups = new ArrayList<String>();

		List<EmployeeUser> employeeUsers = entityManager
				.createNamedQuery("EmployeeUser.findByEmployeeId", EmployeeUser.class)
				.setParameter("employeeId", employeeId).getResultList();

		// 员工拥有的登录账户
		if (employeeUsers != null) {
			employeeUsers.forEach(eu -> {
				usernames.add(eu.getId().getUsername());
				entityManager.createNamedQuery("UserSysrole.findByUsername", UserSysrole.class)
						.setParameter("username", eu.getId().getUsername()).getResultList().forEach(us -> {
							sysroles.add(us.getId().getSysroleId());
						});
			});
		}

		// 员工所在组织列表
		organizations.add(employee.getOrganizationId());

		// 员工担任的岗位列表
		entityManager.createNamedQuery("EmployeeOrganizationRole.findByEmployeeId", EmployeeOrganizationRole.class)
				.setParameter("employeeId", employeeId).getResultList().forEach(f -> {
					rolePKs.add(f.getId().getOrganizationRoleId());
					organizations.add(f.getId().getOrganizationRoleId().getOrganizationId());
				});

		// 员工的级别
		entityManager.createNamedQuery("EmployeeOrganizationLevel.findByEmployeeId", EmployeeOrganizationLevel.class)
				.setParameter("employeeId", employeeId).getResultList().forEach(f -> {
					positionLevelPKs.add(f.getId().getOrganizationLevelId());
//			organizations.add(f.getId().getOrganizationLevelId().getOrganizationId());
				});

		// 员工所在的组
		entityManager.createNamedQuery("GroupEmployee.findByEmployeeId", GroupEmployee.class)
				.setParameter("employeeId", employeeId).getResultList().forEach(f -> {
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

}
