package cn.sparrowmini.permission.config;

import cn.sparrowmini.common.repository.CustomRepositoryFactoryBean;
import cn.sparrowmini.common.util.EntityManagerProvider;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@ComponentScan({"cn.sparrowmini"})
@EntityScan({"cn.sparrowmini"})
@EnableJpaRepositories(basePackages = {"cn.sparrowmini"},repositoryFactoryBeanClass = CustomRepositoryFactoryBean.class)
@Configuration
public class JpaConfig {

    @Autowired
    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        EntityManagerProvider.entityManagerFactory = entityManagerFactory;
    }
}
