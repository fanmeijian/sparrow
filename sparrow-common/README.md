# sparrow-common
test jenkins 1
需要先注入emf才可以使用删除日志功能
public EntityManagerBean(EntityManager emf) {
		EntityManagerHelper.entityManagerFactory = emf.getEntityManagerFactory();
	}

配置命名策略
spring:
jpa:
hibernate:
naming:
physical-strategy: com.example.PrefixPhysicalNamingStrategy

配置前缀
sparrow.hibernate.table.prefix