// package cn.sparrow.permission.mgt.service.impl;

// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
// import java.util.Set;
// import java.util.UUID;

// import javax.persistence.EntityManager;
// import javax.persistence.PersistenceContext;
// import javax.persistence.Table;
// import javax.persistence.metamodel.EntityType;
// import javax.sql.DataSource;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.context.ApplicationContext;
// import org.springframework.context.ConfigurableApplicationContext;
// import org.springframework.jdbc.core.JdbcTemplate;
// import org.springframework.stereotype.Service;
// import org.springframework.web.bind.annotation.GetMapping;

// import cn.sparrow.permission.mgt.api.ModelService;
// import cn.sparrow.permission.mgt.service.repository.ApiRepository;
// import cn.sparrow.permission.mgt.service.repository.ModelAttributeRepository;
// import cn.sparrow.permission.mgt.service.repository.ModelRepository;
// import cn.sparrow.permission.model.resource.Model;
// import cn.sparrow.permission.model.resource.ModelAttribute;
// import cn.sparrow.permission.model.resource.ModelAttributePK;

// @Service
// public class SparrowServiceImpl implements SparrowService{

// 	private static Logger logger = LoggerFactory.getLogger(SparrowServiceImpl.class);

// 	@Autowired
// 	ModelService modelService;

// 	@Autowired
// 	private ConfigurableApplicationContext appContext;

// 	private DataSource ds ;
// 	private JdbcTemplate jdbcTemplate ;
// 	private List<String> urlList = new ArrayList<String>();
// 	private List<String> menuList = new ArrayList<String>();
// 	private String sysroleId = UUID.randomUUID().toString().replaceAll("-", "");

// 	public void init(ConfigurableApplicationContext appContext) {
// 		this.appContext = appContext;
// 		 ds = appContext.getBean(DataSource.class);
// 		 jdbcTemplate = appContext.getBean(JdbcTemplate.class);
// 		initSysrole();
// 		initMenu();
// 		initUrl();
// //		initUrlPermission();
// 		modelService.init();
// 		logger.info("finish model init.");

// 	}

// 	public void initSysrole() {		
// 		jdbcTemplate.execute(
// 				"insert into spr_sysrole(id, name, code,  created_date,created_by, modified_date, modified_by,is_system) values('"
// 						+ sysroleId + "','???????????????','SYSADMIN',now(),'SparrowSystem',now(),'SparrowSystem',true);");
// 		logger.info("Create sysrole SYSADMIN");

// 		jdbcTemplate.execute(
// 				"insert into spr_sysrole(id, name, code, created_date,created_by, modified_date, modified_by,is_system) values('"
// 						+ UUID.randomUUID().toString().replaceAll("-", "") + "','?????????','ADMIN',now(),'SparrowSystem',now(),'SparrowSystem',true);");
// 		logger.info("Create sysrole ADMIN");

// 		jdbcTemplate.execute(
// 				"insert into spr_user_sysrole(username, sysrole_id, created_date,created_by, modified_date, modified_by) values('ROOT','"
// 						+ sysroleId + "',now(),'SparrowSystem',now(),'SparrowSystem');");
// 		logger.info("Grant sysrole SYSADMIN to user ROOT");
		
		
// 	}

// 	public void initUrl() {
		
// 		for (int i = 0; i < 85; i++) {
// 			urlList.add(UUID.randomUUID().toString().replaceAll("-", ""));
// 		}
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(0)+"','sparrow','GET','??????????????????','RESTRICT','/modelAttributes','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(1)+"','sparrow','POST','??????????????????','RESTRICT','/modelAttributes/batch','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(2)+"','sparrow','PATCH','??????????????????','RESTRICT','/modelAttributes/batch','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(3)+"','sparrow','DELETE','??????????????????','RESTRICT','/modelAttributes/batch','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(4)+"','sparrow','POST','????????????????????????','RESTRICT','/modelAttributes/permissions','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(5)+"','sparrow','DELETE','????????????????????????','RESTRICT','/modelAttributes/permissions','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(6)+"','sparrow','GET','????????????','RESTRICT','/sysroles','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(7)+"','sparrow','POST','????????????','RESTRICT','/sysroles/batch','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(8)+"','sparrow','PATCH','????????????','RESTRICT','/sysroles/batch','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(9)+"','sparrow','DELETE','????????????','RESTRICT','/sysroles/batch','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(10)+"','sparrow','GET','????????????','RESTRICT','/sysroles/search/findContain','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(11)+"','sparrow','POST','??????????????????','RESTRICT','/sysroles/permissions','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(12)+"','sparrow','DELETE','??????????????????','RESTRICT','/sysroles/permissions','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(13)+"','sparrow','GET','?????????','RESTRICT','/organizations/getTreeByParentId','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(14)+"','sparrow','GET','????????????????????????','RESTRICT','/organizations/getMyTree','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(15)+"','sparrow','POST','????????????','RESTRICT','/organizations/batch','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(16)+"','sparrow','PATCH','????????????','RESTRICT','/organizations/batch','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(17)+"','sparrow','DELETE','????????????','RESTRICT','/organizations/batch','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(18)+"','sparrow','GET','????????????','RESTRICT','/organizations/search/findContain','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(19)+"','sparrow','POST','??????????????????','RESTRICT','/organization/relations/batch','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(20)+"','sparrow','DELETE','??????????????????','RESTRICT','/organization/relations/batch','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(21)+"','sparrow','GET','?????????','RESTRICT','/roles/getTreeByParentId','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(22)+"','sparrow','POST','????????????','RESTRICT','/roles/batch','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(23)+"','sparrow','PATCH','????????????','RESTRICT','/roles/batch','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(24)+"','sparrow','DELETE','????????????','RESTRICT','/roles/batch','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(25)+"','sparrow','GET','????????????','RESTRICT','/roles/search/findContain','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(26)+"','sparrow','POST','??????????????????','RESTRICT','/roles/relations/batch','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(27)+"','sparrow','DELETE','??????????????????','RESTRICT','/roles/relations/batch','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(28)+"','sparrow','GET','?????????','RESTRICT','/levels/getTreeByParentId','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(29)+"','sparrow','POST','????????????','RESTRICT','/levels/batch','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(30)+"','sparrow','PATCH','????????????','RESTRICT','/levels/batch','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(31)+"','sparrow','DELETE','????????????','RESTRICT','/levels/batch','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(32)+"','sparrow','GET','????????????','RESTRICT','/levels/search/findContain','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(33)+"','sparrow','POST','??????????????????','RESTRICT','/level/relations/batch','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(34)+"','sparrow','DELETE','??????????????????','RESTRICT','/level/relations/batch','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(35)+"','sparrow','GET','????????????','RESTRICT','/models','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(36)+"','sparrow','PATCH','????????????','RESTRICT','/models/batch','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(37)+"','sparrow','POST','????????????','RESTRICT','/models/batch','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(38)+"','sparrow','POST','??????????????????','RESTRICT','/models/permissions','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(39)+"','sparrow','DELETE','??????????????????','RESTRICT','/models/permissions','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(40)+"','sparrow','GET','??????????????????','RESTRICT','/models/permissions','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(41)+"','sparrow','DELETE','????????????','RESTRICT','/models/batch','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(42)+"','sparrow','GET','?????????ID???????????????','RESTRICT','/groups/getTreeByParentId','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(43)+"','sparrow','GET','????????????ID???????????????','RESTRICT','/groups/getTreeByOrganizationId','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(44)+"','sparrow','GET','??????????????????','RESTRICT','/groups/*/members','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(45)+"','sparrow','POST','????????????','RESTRICT','/groups/batch','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(46)+"','sparrow','DELETE','????????????','RESTRICT','/groups/batch','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(47)+"','sparrow','GET','????????????','RESTRICT','/groups/search/findContain','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(48)+"','sparrow','POST','??????????????????','RESTRICT','/groups/members','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(49)+"','sparrow','DELETE','??????????????????','RESTRICT','/groups/members','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(50)+"','sparrow','GET','??????????????????','RESTRICT','/operationLogs','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(51)+"','sparrow','GET','?????????ID???????????????','RESTRICT','/menus/getTreeByParentId','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(52)+"','sparrow','GET','??????????????????????????????','RESTRICT','/menus/getTreeByUsername','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(53)+"','sparrow','GET','????????????ID???????????????','RESTRICT','/menus/getTreeBySysroleId','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(54)+"','sparrow','GET','???????????????????????????','RESTRICT','/menus/getMyTree','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(55)+"','sparrow','POST','????????????','RESTRICT','/menus/batch','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(56)+"','sparrow','POST','??????????????????','RESTRICT','/menus/permissions','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(57)+"','sparrow','DELETE','??????????????????','RESTRICT','/menus/permissions','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(58)+"','sparrow','PATCH','????????????','RESTRICT','/menus/batch','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(59)+"','sparrow','DELETE','????????????','RESTRICT','/menus/batch','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(60)+"','sparrow','POST','??????????????????','RESTRICT','/menus/permissions','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(61)+"','sparrow','DELETE','??????????????????','RESTRICT','/menus/permissions','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(62)+"','sparrow','GET','URL????????????','RESTRICT','/sparrowUrls','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(63)+"','sparrow','POST','??????URL??????','RESTRICT','/sparrowUrls/batch','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(64)+"','sparrow','POST','??????URL??????????????????','RESTRICT','/sparrowUrls/permissions','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(65)+"','sparrow','DELETE','??????URL??????????????????','RESTRICT','/sparrowUrls/permissions','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(66)+"','sparrow','PATCH','??????URL??????','RESTRICT','/sparrowUrls/batch','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(67)+"','sparrow','DELETE','??????URL??????','RESTRICT','/sparrowUrls/batch','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(68)+"','sparrow','GET','??????????????????','RESTRICT','/dataPermissions','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(69)+"','sparrow','POST','??????????????????','RESTRICT','/dataPermissions/batch','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(70)+"','sparrow','DELETE','??????????????????','RESTRICT','/dataPermissions/batch','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(71)+"','sparrow','GET','????????????????????????','RESTRICT','/dataFieldPermissions','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(72)+"','sparrow','POST','????????????????????????','RESTRICT','/dataFieldPermissions/batch','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(73)+"','sparrow','DELETE','????????????????????????','RESTRICT','/dataFieldPermissions/batch','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(74)+"','sparrow','GET','????????????','RESTRICT','/employees/*','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(75)+"','sparrow','GET','?????????','RESTRICT','/employees/getTreeByParentId','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(76)+"','sparrow','POST','????????????','RESTRICT','/employees/batch','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(77)+"','sparrow','PATCH','??????????????????','RESTRICT','/employees/batch','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(78)+"','sparrow','DELETE','????????????','RESTRICT','/employees/batch','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(79)+"','sparrow','POST','??????????????????','RESTRICT','/employees/roles/batch','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(80)+"','sparrow','DELETE','??????????????????','RESTRICT','/employees/roles/batch','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(81)+"','sparrow','POST','??????????????????','RESTRICT','/employees/levels/batch','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(82)+"','sparrow','DELETE','??????????????????','RESTRICT','/employees/levels/batch','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(83)+"','sparrow','POST','??????????????????','RESTRICT','/employees/relations/batch','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		jdbcTemplate.execute("insert into spr_api(id,client_id,method,name,permission,uri,created_by,created_date,modified_by,modified_date,is_system) values('"+urlList.get(84)+"','sparrow','DELETE','??????????????????','RESTRICT','/employees/relations/batch','SparrowSystem',now(),'SparrowSystem',now(),true);");
// 		logger.info("Init Url finished");
// 	}

// 	public void initMenu() {
// 		for (int i = 0; i < 20; i++) {
// 			menuList.add(UUID.randomUUID().toString().replaceAll("-", ""));
// 		}
// 		jdbcTemplate.execute("insert into spr_menu(id, name,code, icon,parent_id, url, created_date, modified_by, modified_date,created_by,is_system) values('"+ menuList.get(0) + "','????????????','001','team',null,'/#',now(),'SparrowSystem',now(),'SparrowSystem',true);");
// 		jdbcTemplate.execute("insert into spr_menu(id, name,code, icon,parent_id, url, created_date, modified_by, modified_date,created_by,is_system) values('"+ menuList.get(1) + "','????????????','002','','"+ menuList.get(0) + "','/organization',now(),'SparrowSystem',now(),'SparrowSystem',true);");
// 		jdbcTemplate.execute("insert into spr_menu(id, name,code, icon,parent_id, url, created_date, modified_by, modified_date,created_by,is_system) values('"+ menuList.get(2) + "','????????????','003','','"+ menuList.get(0) + "','/role',now(),'SparrowSystem',now(),'SparrowSystem',true);");
// 		jdbcTemplate.execute("insert into spr_menu(id, name,code, icon,parent_id, url, created_date, modified_by, modified_date,created_by,is_system) values('"+ menuList.get(3) + "','????????????','004','','"+ menuList.get(0) + "','/level',now(),'SparrowSystem',now(),'SparrowSystem',true);");
// 		jdbcTemplate.execute("insert into spr_menu(id, name,code, icon,parent_id, url, created_date, modified_by, modified_date,created_by,is_system) values('"+ menuList.get(4) + "','????????????','005','','"+ menuList.get(0) + "','/group',now(),'SparrowSystem',now(),'SparrowSystem',true);");
// 		jdbcTemplate.execute("insert into spr_menu(id, name,code, icon,parent_id, url, created_date, modified_by, modified_date,created_by,is_system) values('"+ menuList.get(5) + "','????????????','006','','"+ menuList.get(0) + "','/employee',now(),'SparrowSystem',now(),'SparrowSystem',true);");
// 		jdbcTemplate.execute("insert into spr_menu(id, name,code, icon,parent_id, url, created_date, modified_by, modified_date,created_by,is_system) values('"+ menuList.get(6) + "','????????????','007','safety-certificate',null,'/#',now(),'SparrowSystem',now(),'SparrowSystem',true);");
// 		jdbcTemplate.execute("insert into spr_menu(id, name,code, icon,parent_id, url, created_date, modified_by, modified_date,created_by,is_system) values('"+ menuList.get(7) + "','????????????','008','','"+ menuList.get(6) + "','/url',now(),'SparrowSystem',now(),'SparrowSystem',true);");
// 		jdbcTemplate.execute("insert into spr_menu(id, name,code, icon,parent_id, url, created_date, modified_by, modified_date,created_by,is_system) values('"+ menuList.get(8) + "','????????????','009','','"+ menuList.get(6) + "','/menu',now(),'SparrowSystem',now(),'SparrowSystem',true);");
// 		jdbcTemplate.execute("insert into spr_menu(id, name,code, icon,parent_id, url, created_date, modified_by, modified_date,created_by,is_system) values('"+ menuList.get(9) + "','????????????','010','','"+ menuList.get(6) + "','/sparrowApp',now(),'SparrowSystem',now(),'SparrowSystem',true);");
// 		jdbcTemplate.execute("insert into spr_menu(id, name,code, icon,parent_id, url, created_date, modified_by, modified_date,created_by,is_system) values('"+ menuList.get(10) + "','????????????','011','','"+ menuList.get(6) + "','/sysrole',now(),'SparrowSystem',now(),'SparrowSystem',true);");
// 		jdbcTemplate.execute("insert into spr_menu(id, name,code, icon,parent_id, url, created_date, modified_by, modified_date,created_by,is_system) values('"+ menuList.get(11) + "','????????????','012','','"+ menuList.get(6) + "','/model',now(),'SparrowSystem',now(),'SparrowSystem',true);");
// 		jdbcTemplate.execute("insert into spr_menu(id, name,code, icon,parent_id, url, created_date, modified_by, modified_date,created_by,is_system) values('"+ menuList.get(12) + "','??????????????????','013','','"+ menuList.get(6) + "','/modelPermissionManagement',now(),'SparrowSystem',now(),'SparrowSystem',true);");
// 		jdbcTemplate.execute("insert into spr_menu(id, name,code, icon,parent_id, url, created_date, modified_by, modified_date,created_by,is_system) values('"+ menuList.get(13) + "','??????????????????','014','','"+ menuList.get(6) + "','/dataPermissionManagement',now(),'SparrowSystem',now(),'SparrowSystem',true);");
// 		jdbcTemplate.execute("insert into spr_menu(id, name,code, icon,parent_id, url, created_date, modified_by, modified_date,created_by,is_system) values('"+ menuList.get(14) + "','??????????????????','015','','"+ menuList.get(6) + "','/filePermission',now(),'SparrowSystem',now(),'SparrowSystem',true);");
// 		jdbcTemplate.execute("insert into spr_menu(id, name,code, icon,parent_id, url, created_date, modified_by, modified_date,created_by,is_system) values('"+ menuList.get(15) + "','????????????','016','setting',null,'/#',now(),'SparrowSystem',now(),'SparrowSystem',true);");
// 		jdbcTemplate.execute("insert into spr_menu(id, name,code, icon,parent_id, url, created_date, modified_by, modified_date,created_by,is_system) values('"+ menuList.get(16) + "','????????????','017','','"+ menuList.get(15) + "','/dict',now(),'SparrowSystem',now(),'SparrowSystem',true);");
// 		jdbcTemplate.execute("insert into spr_menu(id, name,code, icon,parent_id, url, created_date, modified_by, modified_date,created_by,is_system) values('"+ menuList.get(17) + "','?????????','018','','"+ menuList.get(15) + "','/serialNumber',now(),'SparrowSystem',now(),'SparrowSystem',true);");
// 		jdbcTemplate.execute("insert into spr_menu(id, name,code, icon,parent_id, url, created_date, modified_by, modified_date,created_by,is_system) values('"+ menuList.get(18) + "','????????????','019','','"+ menuList.get(15) + "','/config',now(),'SparrowSystem',now(),'SparrowSystem',true);");
// 		jdbcTemplate.execute("insert into spr_menu(id, name,code, icon,parent_id, url, created_date, modified_by, modified_date,created_by,is_system) values('"+ menuList.get(19) + "','????????????','020','','"+ menuList.get(15) + "','/log',now(),'SparrowSystem',now(),'SparrowSystem',true);");
// 		logger.info("Init menu finished");
// 		jdbcTemplate.execute("insert into spr_sysrole_menu(sysrole_id, menu_id) values('"+sysroleId+"','"+menuList.get(0)+"')");
// 		jdbcTemplate.execute("insert into spr_sysrole_menu(sysrole_id, menu_id) values('"+sysroleId+"','"+menuList.get(6)+"')");
// 		jdbcTemplate.execute("insert into spr_sysrole_menu(sysrole_id, menu_id) values('"+sysroleId+"','"+menuList.get(15)+"')");

// 	}

// 	public void initUrlPermission() {
// 		for (String url : urlList) {
// 			jdbcTemplate.execute(
// 					"insert into spr_sysrole_api(api_id, sysrole_id, created_date, modified_by, modified_date,created_by) values('" + url
// 							+ "','" + sysroleId  + "',now(),'SparrowSystem',now(),'SparrowSystem');");
// 		}
// 	}

// 	@Autowired
// 	ModelRepository modelRepository;

// 	@Autowired
// 	ModelAttributeRepository modelAttributeRepository;

// 	@Autowired
// 	ApiRepository urlRepository;

// 	@PersistenceContext
// 	EntityManager entityManager;
// 	@Autowired
// 	private ApplicationContext applicationContext;
	

// 	@GetMapping("/models/syncToTable")
// 	public Map<String, List<String>> syncToTable() {
// 		Set<EntityType<?>> entityTypes = entityManager.getMetamodel().getEntities();
// 		Map<String, List<String>> entitiesMap = new HashMap<String, List<String>>();

// 		entityTypes.forEach(e -> {
// 			Model model = modelRepository.save(new Model(e.getJavaType().getName()));

// 			List<String> attributes = new ArrayList<String>();
// 			e.getAttributes().forEach(a -> {
// 				attributes.add(a.getName());

// 				modelAttributeRepository.save(new ModelAttribute(new ModelAttributePK(a.getName(), model.getName()),
// 						a.getJavaType().getName()));

// 			});
// 			entitiesMap.put(e.getJavaType().getName(), attributes);

// 		});
// 		return entitiesMap;

// 	}

// 	@GetMapping("/models/allEntities")
// 	public Map<String, Object> allEntities() {
// 		Map<String, Object> beans = applicationContext.getBeansWithAnnotation(Table.class);
// 		return beans;
// 	}

// }
