package cn.sparrowmini.common.repository;

import cn.sparrowmini.common.antlr.PredicateBuilder;
import cn.sparrowmini.common.util.JsonUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;

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

    default List<ID> upsert(List<Map<String, Object>> entitiesMap) {
        List<T> entities = new ArrayList<>();
        entitiesMap.forEach(entityMap -> {
            ObjectMapper mapper = JsonUtils.getMapper();
            String idFieldName = idFieldName();
            Object idRaw = entityMap.get(idFieldName);
            Class<ID> idClass = idType();
            if (idRaw != null) {
                ID id = mapper.convertValue(idRaw, idClass);
                if (existsById(id)) {
                    Map<String, Object> patchCopy = new HashMap<>(entityMap);
                    patchCopy.remove(idFieldName);
                    T referenceById = getReferenceById(id);
                    try {
                        mapper.readerForUpdating(referenceById)
                                .readValue(mapper.writeValueAsString(patchCopy));
                        entities.add(referenceById);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Patch更新失败" + e.getOriginalMessage(), e);
                    }
                }else{
                    T newEntity = mapper.convertValue(entityMap, domainType());
                    entities.add(newEntity);
                }

            } else {
                T newEntity = mapper.convertValue(entityMap, domainType());
                entities.add(newEntity);
            }

        });

        saveAll(entities);
        return entities.stream().map(this::getId).toList();

    }


    default Specification<T> filterSpecification(String filter) {
        return new Specification<T>() {
            @Override
            public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                return PredicateBuilder.buildPredicate(filter, criteriaBuilder, root);
            }
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
}
