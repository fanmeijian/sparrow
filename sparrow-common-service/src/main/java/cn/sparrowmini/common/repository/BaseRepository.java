package cn.sparrowmini.common.repository;

import cn.sparrowmini.common.CurrentUser;
import cn.sparrowmini.common.antlr.PredicateBuilder;
import cn.sparrowmini.common.model.BaseOpLog_;
import cn.sparrowmini.common.model.BaseState;
import cn.sparrowmini.common.util.JsonUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.*;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.*;

@NoRepositoryBean
public interface BaseRepository<T, ID>
        extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {

    default Page<T> findAll(Pageable pageable, String filter) {
        Specification<T> specification = filter == null ? Specification.where(null) : filterSpecification(filter);

        return findBy(
                specification,
                query -> query.page(pageable)
        );
    }

    /**
     * 根据Id返回projectionClass
     *
     * @param id
     * @param projectionClass
     * @param <S>
     * @return
     */
    <S> Optional<S> findByIdProjection(ID id, Class<S> projectionClass);

    <U> Page<U> findByProjection(Pageable pageable, String filter, Class<U> projectionClass);

    <P> Page<P> findByProjection(Pageable pageable, Predicate predicate, Class<P> projectionClass);

    <P> Page<P> findByProjection(Pageable pageable, Specification<T> spec, Class<P> projectionClass);

    default Specification<T> isAuthor() {
        return specificationEqual(BaseOpLog_.CREATED_BY, CurrentUser.get());
    }

    @Transactional
    default void updateStat(String stat, List<ID> ids){
        List<T> refs = ids.stream().map(id-> {
             T ref = getReferenceById(id);
             ((BaseState)ref).setStat(stat);
             return ref;
         }).toList();
        saveAll(refs);
    }

    /**
     * 根据条件返回projectClass的列表
     * 没有用jpa的findBy, 是因为jpa的findBy还是会先查出来所有的字段。
     *
     * @param pageable
     * @param filter
     * @param projectionClass
     * @param <P>
     * @return
     */
    default <U> Page<U> findAllProjection(Pageable pageable, String filter, Class<U> projectionClass) {
        Specification<T> specification = filter == null ? Specification.where(null) : filterSpecification(filter);
        return findBy(
                specification,
                query -> query.as(projectionClass).page(pageable)
        );
    }


//    <P> Page<P> findAllProjection(Pageable pageable, String filter, Class<P> projectionClass);


    /**
     * 返回projectClass的列表
     *
     * @param pageable
     * @param projectionClass
     * @param <U>
     * @return
     */
    default <U> Page<U> findAllProjection(Pageable pageable, Class<U> projectionClass) {
        return findAllProjection(pageable, null, projectionClass);
    }

    @Transactional
    List<ID> upsert(List<Map<String, Object>> entitiesMap);

    default Specification<T> filterSpecification(String filter) {
        return new Specification<T>() {
            @Override
            public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                return PredicateBuilder.buildPredicate(filter, criteriaBuilder, root);
            }
        };
    }

    default Specification<T> specificationIn(String field, Collection<?> collection) {
        return (root, query, cb) -> {
            Path<?> path = root;
            // 支持 a.b.c 形式的路径
            for (String part : field.split("\\.")) {
                path = path.get(part);
            }
            return path.in(collection);
        };
    }


    default Specification<T> specificationEqual(String field, Object value) {
        return (root, query, cb) -> {
            Path<?> path = root;
            // 支持 a.b.c 形式的路径
            for (String part : field.split("\\.")) {
                path = path.get(part);
            }
            return cb.equal(path, value);
        };
    }

    @SuppressWarnings("unchecked")
    default ID getId(T entity) {
        BeanWrapper wrapper = new BeanWrapperImpl(entity);
        return (ID) wrapper.getPropertyValue(idFieldName());
    }

    @NonNull
    Class<T> domainType();

    @NonNull
    Class<ID> idType();

    @NonNull
    String idFieldName();

    @NonNull
    Field idField();
}
