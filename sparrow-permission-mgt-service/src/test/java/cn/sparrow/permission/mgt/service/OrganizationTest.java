package cn.sparrow.permission.mgt.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Pageable;

import cn.sparrow.permission.constant.GroupTypeEnum;
import cn.sparrow.permission.constant.OrganizationTypeEnum;
import cn.sparrow.permission.core.api.AuditLogService;
import cn.sparrow.permission.core.api.PermissionService;
import cn.sparrow.permission.core.service.AuditLogServiceImpl;
import cn.sparrow.permission.core.service.PermissionServiceImpl;
import cn.sparrow.permission.mgt.api.EmployeeService;
import cn.sparrow.permission.mgt.api.GroupService;
import cn.sparrow.permission.mgt.api.OrganizationService;
import cn.sparrow.permission.mgt.api.PositionLevelService;
import cn.sparrow.permission.mgt.api.RoleService;
import cn.sparrow.permission.model.group.Group;
import cn.sparrow.permission.model.organization.Employee;
import cn.sparrow.permission.model.organization.Organization;
import cn.sparrow.permission.model.organization.OrganizationPositionLevelPK;
import cn.sparrow.permission.model.organization.OrganizationRelation;
import cn.sparrow.permission.model.organization.OrganizationRole;
import cn.sparrow.permission.model.organization.OrganizationRolePK;
import cn.sparrow.permission.model.organization.PositionLevel;
import cn.sparrow.permission.model.organization.Role;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest
public class OrganizationTest {

	@Autowired
	private static TestEntityManager entityManager;

	@TestConfiguration
	static class PermissionServiceImplTestContextConfiguration {
		@Bean
		public PermissionService permissionService() {
			return new PermissionServiceImpl((EntityManager) entityManager);
		}
		
		@Bean
		public AuditLogService auditLogService() {
			return new AuditLogServiceImpl((EntityManager) entityManager);
		}
	}

//	TestEntityManager entityManager = new PermissionServiceImpl((EntityManager) entityManager)

//	@Autowired
//	OrganizationRelationRepository organizationRelationRepository;
//	@Autowired
//	EmployeeRepository employeeRepository;
//	@Autowired
//	RoleRepository roleRepository;
//	@Autowired
//	PositionLevelRepository positionLevelRepository;
//	@Autowired
//	GroupRepository groupRepository;

	@Autowired
	OrganizationService organizationService;
	@Autowired
	RoleService roleService;// = new RoleServiceImpl();
	@Autowired
	PositionLevelService positionLevelService;// = new PositionLevelServiceImpl();
	@Autowired
	GroupService groupService;// = new GroupServiceImpl();
	@Autowired
	EmployeeService employeeService;// = new EmployeeServiceImpl();

	@Test
	@Transactional
	void orgTest() {
		Group group1 = new Group("g1", "g1");
		groupService.create(group1);

		Group group2 = new Group("g2", "g2");
		groupService.create(group2);

		Organization organization = new Organization("ooooooo1", "ooooooo1", OrganizationTypeEnum.ORGANIZATION);
		organizationService.create(organization);
		List<String> orgs = new ArrayList<>();
		orgs.add(organization.getId());

		// ??????????????????
		groupService.setParentOrgs(group1.getId(), orgs);
		groupService.setParentOrgs(group2.getId(), orgs);

		String prev = null;

		for (int i = 0; i < 6; i++) {
			Organization parent = organizationService
					.create(new Organization("o" + i, i + "", OrganizationTypeEnum.ORGANIZATION));
			prev = parent.getId();

			List<String> ids = new ArrayList<>();
			ids.add(parent.getId());

			for (int j = 0; j < 6; j++) {
				// ??????1???????????????
				Organization dept1 = organizationService.create(
						new Organization("d" + i + "" + j, "o" + i + "d" + i + "" + j, OrganizationTypeEnum.UNIT));

				// ????????????????????????
				organizationService.addParent(dept1.getId(), ids);

				// ??????2?????????
				Organization org1 = organizationService.create(new Organization("o" + i + "" + j,
						"o" + i + "o" + i + "" + j, OrganizationTypeEnum.ORGANIZATION));
				organizationService.addParent(org1.getId(), Arrays.asList(new String[] { parent.getId() }));

				// ??????????????????
				Employee employee1 = new Employee("e1" + i + "" + j, "o" + i + "e1" + i + "" + j, parent.getId());
				employeeService.create(employee1);
				Employee employee2 = new Employee("e2" + i + "" + j, "o" + i + "e2" + i + "" + j, parent.getId());
				employeeService.create(employee2);

				// ????????????
				Role role = new Role(j + "r1" + i, j + "o" + i + "r1" + i);
				roleService.create(role);

				// ??????????????????????????????
				roleService.setParentOrg(role.getId(), Arrays.asList(new String[] { parent.getId() }));

				// ????????????????????????
				boolean flagHasParentOrg = false;
				for(OrganizationRole organizationRole:roleService.getParentOrganizations(role.getId())) {
					if(organizationRole.getId().getOrganizationId().equals(parent.getId())) {
						flagHasParentOrg = true;
					}
				}
				assertEquals(true, flagHasParentOrg);

				// ????????????????????????
				roleService.removeParentOrg(role.getId(), Arrays.asList(new String[] { parent.getId() }));
				flagHasParentOrg = false;
				for(OrganizationRole organizationRole:roleService.getParentOrganizations(role.getId())) {
					if(organizationRole.getId().getOrganizationId().equals(parent.getId())) {
						flagHasParentOrg = true;
					}
				}
				assertEquals(false, flagHasParentOrg);

				roleService.setParentOrg(role.getId(), Arrays.asList(new String[] { parent.getId() }));

				// ????????????
				roleService.update(role.getId(), Map.of("name", "update" + j));
				assertEquals("update" + j, roleService.get(role.getId()).getName());

				// ????????????
				Role roleParent = new Role(j + "pr1" + i, j + "po" + i + "r1" + i);
				roleService.create(roleParent);
				roleService.setParentOrg(roleParent.getId(), Arrays.asList(new String[] { parent.getId() }));
				// ??????????????????
				roleService.addParents(new OrganizationRolePK(parent.getId(), role.getId()), Arrays.asList(
						new OrganizationRolePK[] { new OrganizationRolePK(parent.getId(), roleParent.getId()) }));
				// ??????????????????
				assertEquals(1, roleService.getParents(parent.getId(), role.getId()).size());
				// ??????????????????
				assertEquals(1, roleService.getChildren(parent.getId(), roleParent.getId()).size());

				// ??????????????????
				roleService.delParents(new OrganizationRolePK(parent.getId(), role.getId()), Arrays.asList(
						new OrganizationRolePK[] { new OrganizationRolePK(parent.getId(), roleParent.getId()) }));
				assertEquals(0, roleService.getParents(parent.getId(), role.getId()).size());

				// ????????????????????????
				employeeService.addRole(employee2.getId(), Arrays.asList(
						new OrganizationRolePK[] { new OrganizationRolePK(parent.getId(), roleParent.getId()) }));

				// ????????????????????????
				assertEquals(1, employeeService.getRoles(employee2.getId()).size());

				// ????????????????????????
				employeeService.removeRole(employee2.getId(), Arrays.asList(
						new OrganizationRolePK[] { new OrganizationRolePK(parent.getId(), roleParent.getId()) }));
				assertEquals(0, employeeService.getRoles(employee2.getId()).size());
//				employeeService.addRole(employee2.getId(), Arrays.asList(
//						new OrganizationRolePK[] { new OrganizationRolePK(parent.getId(), roleParent.getId()) }));
				
				// ??????????????????
				employeeService.addParent(employee1.getId(), Arrays.asList(new String[] { employee2.getId() }));
				// ??????????????????
				assertEquals(1, employeeService.getParents(employee1.getId()).size());
				// ??????????????????
				assertEquals(1, employeeService.getChildren(employee2.getId()).size());
				// ??????????????????
				employeeService.removeParent(employee1.getId(), Arrays.asList(new String[] { employee2.getId() }));
				assertEquals(0, employeeService.getParents(employee1.getId()).size());

				// ????????????
				PositionLevel positionLevel = new PositionLevel(j + "p" + i, j + "o" + i + "p" + i);
				positionLevelService.create(positionLevel);
				PositionLevel positionLevel2 = new PositionLevel(j + "2p" + i, j + "2o" + i + "p" + i);
				positionLevelService.create(positionLevel2);
				// ????????????????????????
				positionLevelService.setParentOrg(positionLevel.getId(), ids);
				positionLevelService.setParentOrg(positionLevel2.getId(), ids);
				// ????????????????????????
				assertEquals(1, positionLevelService.getParentOrganizations(positionLevel.getId()).size());
				// ????????????????????????
				positionLevelService.removeParentOrg(positionLevel.getId(), ids);
				assertEquals(0, positionLevelService.getParentOrganizations(positionLevel.getId()).size());
				positionLevelService.setParentOrg(positionLevel.getId(), ids);
				// ??????????????????
				positionLevelService.addRelation(new OrganizationPositionLevelPK(parent.getId(), positionLevel.getId()),
						Arrays.asList(new OrganizationPositionLevelPK[] {
								new OrganizationPositionLevelPK(parent.getId(), positionLevel2.getId()) }));
				// ??????????????????
				assertEquals(1, positionLevelService
						.getParents(new OrganizationPositionLevelPK(parent.getId(), positionLevel.getId())).size());
				// ??????????????????
				assertEquals(1, positionLevelService
						.getChildren(new OrganizationPositionLevelPK(parent.getId(), positionLevel2.getId())).size());
				// ??????????????????
				positionLevelService.removeRelation(
						new OrganizationPositionLevelPK(parent.getId(), positionLevel.getId()),
						Arrays.asList(new OrganizationPositionLevelPK[] {
								new OrganizationPositionLevelPK(parent.getId(), positionLevel2.getId()) }));
				assertEquals(0, positionLevelService
						.getParents(new OrganizationPositionLevelPK(parent.getId(), positionLevel.getId())).size());

				// ??????????????????
				employeeService.addLevel(employee1.getId(), Arrays.asList(new OrganizationPositionLevelPK[] {
						new OrganizationPositionLevelPK(parent.getId(), positionLevel2.getId()) }));
				// ????????????????????????
				assertEquals(1, positionLevelService
						.getEmployees(new OrganizationPositionLevelPK(parent.getId(), positionLevel2.getId())).size());
				// ????????????????????????
				assertEquals(1, employeeService.getLevels(employee1.getId()).size());
				// ??????????????????
				employeeService.removeLevel(employee1.getId(), Arrays.asList(new OrganizationPositionLevelPK[] {
						new OrganizationPositionLevelPK(parent.getId(), positionLevel2.getId()) }));
				assertEquals(0, positionLevelService
						.getEmployees(new OrganizationPositionLevelPK(parent.getId(), positionLevel2.getId())).size());
				assertEquals(0, employeeService.getLevels(employee1.getId()).size());
//				employeeService.addLevel(employee1.getId(), Arrays.asList(new OrganizationPositionLevelPK[] {
//						new OrganizationPositionLevelPK(parent.getId(), positionLevel2.getId()) }));

				// ????????????
				Group group = new Group(j + "g" + i, j + "o" + i + "g" + i);
				groupService.create(group);
				groupService.setParentOrgs(group.getId(), ids);

				for (int n = 0; n < 6; n++) {
					// ????????????
					Organization dept2 = organizationService.create(new Organization("d1" + i + "" + j + "" + n,
							"o" + i + "d1" + i + "" + j + "" + n, OrganizationTypeEnum.UNIT));
					// ????????????
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("name", "update" + n);
					organizationService.update(dept2.getId(), map);
					assertEquals("update" + n, organizationService.get(dept2.getId()).getName());
					// ????????????????????????
					organizationService.addParent(dept2.getId(), Arrays.asList(new String[] { dept1.getId() }));

					// ????????????
					boolean flagHasParent = false;
					for (OrganizationRelation org:organizationService.getParents(dept2.getId())) {
						if(org.getId().getParentId().equals(dept1.getId())) {
							flagHasParent = true;
						}
					}
					assertEquals(true,flagHasParent);

					Organization dept3 = organizationService.create(new Organization("d2" + i + "" + j + "" + n,
							"o" + i + "d2" + i + "" + j + "" + n, OrganizationTypeEnum.UNIT));
					organizationService.addParent(dept3.getId(), Arrays.asList(new String[] { org1.getId() }));
					// ????????????
					assertEquals(true, organizationService.getChildren(org1.getId())
							.contains(organizationService.get(dept3.getId())));

					// ????????????
					organizationService.removeParent(dept3.getId(), Arrays.asList(new String[] { org1.getId() }));
					assertEquals(false, organizationService.getChildren(org1.getId())
							.contains(organizationService.get(dept3.getId())));
				}

				// ????????????
				groupService.addMembers(group1.getId(), GroupTypeEnum.EMPLOYEE,
						Arrays.asList(new Object[] { employee1.getId() }));

				assertEquals(true, groupService.getMembers(group1.getId(), GroupTypeEnum.EMPLOYEE, Pageable.unpaged())
						.getTotalElements()>0);
				// ????????????
				groupService.addMembers(group1.getId(), GroupTypeEnum.GROUP, Arrays.asList(new Object[] {group2.getId()}));
				assertEquals(1, groupService.getMembers(group1.getId(), GroupTypeEnum.GROUP, Pageable.unpaged()).getTotalElements());
				// ????????????
				groupService.removeMembers(group1.getId(), GroupTypeEnum.GROUP, Arrays.asList(new Object[] {group2.getId()}));
				assertEquals(0, groupService.getMembers(group1.getId(), GroupTypeEnum.GROUP, Pageable.unpaged()).getTotalElements());

				// ????????????
				groupService.addMembers(group1.getId(), GroupTypeEnum.ORGANIZATION,
						Arrays.asList(new Object[] { org1.getId() }));
				assertEquals(1, groupService.getMembers(group1.getId(), GroupTypeEnum.ORGANIZATION, Pageable.unpaged()).getTotalElements());
				groupService.removeMembers(group1.getId(), GroupTypeEnum.ORGANIZATION, Arrays.asList(new Object[] {org1.getId()}));
				assertEquals(0, groupService.getMembers(group1.getId(), GroupTypeEnum.ORGANIZATION, Pageable.unpaged()).getTotalElements());
				
				// ????????????
				groupService.addMembers(group1.getId(), GroupTypeEnum.ROLE,
						Arrays.asList(new Object[] { role.getId() }));
				assertEquals(1, groupService.getMembers(group1.getId(), GroupTypeEnum.ROLE, Pageable.unpaged()).getTotalElements());
				groupService.removeMembers(group1.getId(), GroupTypeEnum.ROLE, Arrays.asList(new Object[] {role.getId()}));
				assertEquals(0, groupService.getMembers(group1.getId(), GroupTypeEnum.ROLE, Pageable.unpaged()).getTotalElements());
				
				// ????????????
				groupService.addMembers(group1.getId(), GroupTypeEnum.LEVEL,
						Arrays.asList(new Object[] { positionLevel.getId() }));
				assertEquals(1, groupService.getMembers(group1.getId(), GroupTypeEnum.LEVEL, Pageable.unpaged()).getTotalElements());
				groupService.removeMembers(group1.getId(), GroupTypeEnum.LEVEL, Arrays.asList(new Object[] {positionLevel.getId()}));
				assertEquals(0, groupService.getMembers(group1.getId(), GroupTypeEnum.LEVEL, Pageable.unpaged()).getTotalElements());

				
			
			}

			// ???????????????
			assertEquals(12, organizationService.getTreeByParentId(parent.getId()).getChildren().size());
//			log.info("======={},{}", organizationService.getTreeByParentId(null).getChildren().size(),
//					organizationService.getTreeByParentId(parent.getId()).getChildren().size());
		}

		// log.info("{}", organizationServiceImpl.getChildren(prev).size());
		assertEquals(12, organizationService.getChildren(prev).size());
		assertEquals(12, organizationService.getEmployees(prev, Pageable.unpaged()).getTotalElements());
		assertEquals(12, organizationService.getRoles(prev, Pageable.unpaged()).getTotalElements());
		assertEquals(12, organizationService.getLevels(prev, Pageable.unpaged()).getTotalElements());
		assertEquals(6, organizationService.getGroups(prev, Pageable.unpaged()).getTotalElements());

		
		
		// ???????????????
		for(int i=0;i <3;i++) {
			Group groupL1 = new Group("","o"+i);
			groupService.create(groupL1);
			for(int j=0;j<3;j++) {
				Group groupL2 = new Group("","o"+i +"" +j);
				groupService.create(groupL2);
				groupService.addMembers(groupL2.getId(), GroupTypeEnum.GROUP, Arrays.asList(new Object[] {groupL1.getId()}));
				for(int n=0;n<3;n++) {
					Group groupL3 = new Group("","o"+i +"" +j +""+n);
					groupService.create(groupL3);
					groupService.addMembers(groupL3.getId(), GroupTypeEnum.GROUP, Arrays.asList(new Object[] {groupL2.getId()}));
				}
			}
//			log.info("=========={}",groupService.getTree(groupL1.getId()));
			assertEquals(0, groupService.getTree(groupL1.getId()).getChildren().size());

		}
		
	}
}
