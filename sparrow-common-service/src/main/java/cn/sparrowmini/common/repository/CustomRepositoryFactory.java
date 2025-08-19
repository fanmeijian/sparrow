package cn.sparrowmini.common.repository;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;

import java.io.Serializable;

/**
 * 主要为了获取projectionFactory,用于构造通过接口构造DTO
 */
public class CustomRepositoryFactory extends JpaRepositoryFactory {

    private final EntityManager em;
    private final ProjectionFactory projectionFactory;

    public CustomRepositoryFactory(EntityManager em, BeanFactory beanFactory) {
        super(em);
        this.em = em;
        this.projectionFactory = getProjectionFactory();
    }

    @Override
    protected BaseRepositoryImpl<?, ?> getTargetRepository(RepositoryInformation information, EntityManager entityManager) {
        JpaEntityInformation<?, Serializable> entityInformation =
                getEntityInformation(information.getDomainType());
        return new BaseRepositoryImpl<>(entityInformation, entityManager, projectionFactory);
    }

    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
        return BaseRepositoryImpl.class;
    }
}
