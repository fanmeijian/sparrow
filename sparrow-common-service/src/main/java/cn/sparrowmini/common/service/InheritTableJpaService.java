package cn.sparrowmini.common.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InheritTableJpaService <T, ID extends Serializable> {
    T save(T entity);

    List<T> saveAll(List<T> entities);

    Optional<T> findById(ID id);

    Page<T> findAll(Pageable pageable, String filter);

    Page<T> findAll(Pageable pageable, Specification<T> specification);

    void deleteById(ID id);

    void deleteByIds(Collection<ID> ids);
}