package cn.sparrowmini.server.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import cn.sparrowmini.common.repository.CustomRepositoryFactoryBean;
import cn.sparrowmini.common.util.EntityManagerProvider;
import jakarta.persistence.EntityManagerFactory;


@EntityScan(basePackages = {"cn.sparrowmini", "cn.liyuan.chnplc"})
@ComponentScan(basePackages = {
        "cn.sparrowmini",
        "cn.liyuan.dengbo",
})
@EnableJpaRepositories(basePackages = {"cn.sparrowmini", "cn.liyuan.chnplc"}, repositoryFactoryBeanClass = CustomRepositoryFactoryBean.class)
@Configuration
public class EntityConfig {
    @Autowired
    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        EntityManagerProvider.entityManagerFactory = entityManagerFactory;
    }
}
