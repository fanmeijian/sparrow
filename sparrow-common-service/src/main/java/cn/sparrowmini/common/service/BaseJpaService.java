package cn.sparrowmini.common.service;

import cn.sparrowmini.common.repository.BaseRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.support.Repositories;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public abstract class BaseJpaService<T, ID extends Serializable> implements JpaService<T, ID> {
    // 这里建议使用 ApplicationContext 来获取 Repository，比手动管理 List 更稳健
    private final ApplicationContext applicationContext;
    private final Class<T> entityClass;

    @SuppressWarnings("unchecked")
    public BaseJpaService(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        // 自动解析泛型 T 的类型
        this.entityClass = (Class<T>) GenericTypeResolver.resolveTypeArguments(getClass(), BaseJpaService.class)[0];
    }

    // 核心实现：获取对应的 Repository
    @SuppressWarnings("unchecked")
    protected BaseRepository<T, ID> getJpaRepository() {
        // 利用 Spring Data 的 Repositories 工具类来查找
        Repositories repositories = new Repositories(applicationContext);
        return (BaseRepository<T, ID>) repositories.getRepositoryFor(entityClass)
                .orElseThrow(() -> new IllegalStateException("未找到实体 " + entityClass.getSimpleName() + " 对应的 Repository"));
    }

    @Transactional
    public T save(T entity) {
        return getJpaRepository().save(entity);
    }

    @Transactional
    public List<T> saveAll(List<T> entities) {
        return getJpaRepository().saveAll(entities);
    }

    public Optional<T> findById(ID id) {
        return getJpaRepository().findById(id);
    }

    public Page<T> findAll(Pageable pageable, String filter) {
        return getJpaRepository().findAll(pageable, filter);
    }

    public Page<T> findAll(Pageable pageable, Specification<T> specification) {
        return getJpaRepository().findAll(specification, pageable);
    }

    @Transactional
    public void deleteById(ID id) {
        getJpaRepository().deleteById(id);
    }

    @Transactional
    public void deleteByIds(Collection<ID> ids) {
        getJpaRepository().deleteAllById(ids);
    }


}
