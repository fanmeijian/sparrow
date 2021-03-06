package cn.sparrow.permission.mgt;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import cn.sparrow.permission.core.api.AuditLogService;
import cn.sparrow.permission.core.api.EmployeeTokenService;
import cn.sparrow.permission.core.api.PermissionService;
import cn.sparrow.permission.core.service.AuditLogServiceImpl;
import cn.sparrow.permission.core.service.EmployeeTokenServiceImpl;
import cn.sparrow.permission.core.service.PermissionServiceImpl;

@Configuration
public class EntityManagerFactoryConfiguration {

	@Autowired
	public EntityManagerFactoryConfiguration(LocalContainerEntityManagerFactoryBean entityManagerFactory) {

		Map<String, Object> map = new HashMap<String, Object>();
//		entityManagerFactory.getProperties().entrySet().forEach(f -> {
//			map.put(f.getKey(), f.getValue());
//		});
		LocalContainerEntityManagerFactoryBean o =(LocalContainerEntityManagerFactoryBean)entityManagerFactory;
		LocalContainerEntityManagerFactoryBean o1= new LocalContainerEntityManagerFactoryBean();
		o1.setJpaVendorAdapter(o.getJpaVendorAdapter());
		o1.setDataSource(o.getDataSource());
		o1.setJpaPropertyMap(o.getJpaPropertyMap());
		o1.setPackagesToScan("cn.sparrow.permission.*");
		o1.afterPropertiesSet();
		
//		Map<String, String> properties = new HashMap<String, String>();
//		properties.put("javax.persistence.jdbc.url", "jdbc:h2:mem:spr;DB_CLOSE_DELAY=-1");
//		properties.put("javax.persistence.jdbc.driver", "org.h2.Driver");
//		properties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
//		properties.put("hibernate.show_sql", "true");
//		properties.put("javax.persistence.provider", "org.hibernate.jpa.HibernatePersistenceProvider");

//		new HibernatePersistenceProvider().createEntityManagerFactory(persistenceUnitName, properties)
//		EntityManagerFactory entityManagerFactory1 = Persistence.createEntityManagerFactory("cn.sparrow.permission.domain", properties);
//		map.put("javax.persistence.provider", "org.hibernate.jpa.HibernatePersistenceProvider");
//		CurrentEntityManagerFactory.INSTANCE.set(Persistence.createEntityManagerFactory(
//				entityManagerFactory.getProperties().get("hibernate.ejb.persistenceUnitName").toString(),map));
//		CurrentEntityManagerFactory.INSTANCE.set( entityManagerFactory1);
//		System.out.println("*************" + CurrentEntityManagerFactory.INSTANCE.toString());
	}
	
//	@Bean
//	public EntityManagerFactory entityManagerFactory() throws PropertyVetoException {
//	    HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
//	    vendorAdapter.setGenerateDdl(true);
//	    LocalContainerEntityManagerFactoryBean factory;
//	    factory = new LocalContainerEntityManagerFactoryBean();
//	    factory.setPackagesToScan("cn.sparrow.permission.*");
//	    return factory.getNativeEntityManagerFactory();
//	}
	

//	@Bean
//	public PermissionTokenService permissionTokenService() {
//		return new PermissionTokenServiceImpl();
//
//	}

	@Bean
	public PermissionService permissionService(EntityManager entityManager) {
		return new PermissionServiceImpl(entityManager);
	}

	@Bean
	public EmployeeTokenService employeeTokenService(EntityManager entityManager) {
		return new EmployeeTokenServiceImpl(entityManager);
	}
	
	@Bean
	public AuditLogService auditLogService(EntityManager entityManager) {
		return new AuditLogServiceImpl(entityManager);
	}
}
