package cn.sparrowmini.common.service;

import cn.sparrowmini.common.antlr.PredicateBuilder;
import cn.sparrowmini.common.util.JpaUtils;
import cn.sparrowmini.common.util.JsonUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.data.jpa.support.PageableUtils;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static cn.sparrowmini.common.util.JpaUtils.convertToPkValue;
import static cn.sparrowmini.common.util.JpaUtils.findPrimaryKeyField;

@Service
@RequiredArgsConstructor
public class CommonJpaServiceImpl implements CommonJpaService {
    @PersistenceContext
    private final EntityManager entityManager;
    private final ObjectMapper objectMapper = JsonUtils.getMapper();

    @Transactional
    @Override
    public <T, ID> List<ID> upsertEntity(Class<T> clazz, List<Map<String, Object>> entities) {
        List<ID> ids = new ArrayList<>();
        entities.forEach(entity -> {
            Field pkField = JpaUtils.getIdField(clazz);
            String pkFieldName = pkField.getName();
            Object id = entity.get(pkFieldName);
            Map<String, Object> patchMap = new HashMap<>(entity);
            GeneratedValue generatedValue = pkField.getAnnotation(GeneratedValue.class);
            if (generatedValue != null) {
                patchMap.remove(pkFieldName); // 再加一行，确保主键不会被误覆盖
            }

            try {
                ID pkValue = (ID) JpaUtils.convertToPkValue(id, clazz);
                T objectRef = entityManager.getReference(clazz, pkValue);
                System.out.println(objectRef);
                // 将 patch 字段合并进 reference 实体（只会触发一次 UPDATE）
                try {
                    objectMapper
                            .readerForUpdating(objectRef)
                            .readValue(objectMapper.writeValueAsString(patchMap));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                ids.add(pkValue);
            } catch (EntityNotFoundException | IllegalArgumentException e) {
                T newEntity = objectMapper.convertValue(patchMap, clazz);
                entityManager.persist(newEntity);
                pkField.setAccessible(true);
                try {
                    ids.add((ID) pkField.get(newEntity));
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        return ids;
    }

    @Transactional
    @Override
    public <T, ID> long deleteEntity(Class<T> clazz, Collection<ID> ids) {
        long i = 0;
        Class<ID> idClass = (Class<ID>) JpaUtils.getIdType(clazz);
        for (ID id : ids) {
            ID id_ = objectMapper.convertValue(id, idClass);
            T entityRef = null;
            try {
                entityRef = entityManager.getReference(clazz, id_);
                entityManager.remove(entityRef);
                i++;
            } catch (EntityNotFoundException e) {
                ;
            }
        }
        return i;
    }

    @Transactional
    @Override
    public <T, ID> T getEntity(Class<T> clazz, ID id_) {
        Class<?> idClass = JpaUtils.getIdType(clazz);
        Object id = objectMapper.convertValue(id_, idClass);
        return entityManager.find(clazz, id);
    }


    @Transactional
    @Override
    public <T> Page<T> getEntityList(Class<T> clazz, Pageable pageable, String filter) {
        TypedQuery<T> query = this.getQuery(filter, pageable, clazz);
        if (pageable.isPaged()) {
            query.setFirstResult(PageableUtils.getOffsetAsInteger(pageable));
            query.setMaxResults(pageable.getPageSize());
        }

        return PageableExecutionUtils.getPage(query.getResultList(), pageable, () -> {
            return this.getCountQuery(filter, clazz).getSingleResult();
        });

    }

    @Transactional
    @Override
    public <T, ID> List<ID> saveEntity(Class<T> clazz,List<T> entities) {
        List<ID> ids = new ArrayList<>();
        entities.forEach(entity-> {
            entityManager.persist(entity);
            Field pkField = JpaUtils.getIdField(clazz);
            pkField.setAccessible(true);
            try {
                ids.add((ID) pkField.get(entity));
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
        });
        return ids;
    }

    private <T> TypedQuery<Long> getCountQuery(String filter, Class<T> domainClass) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<?> countRoot = countQuery.from(domainClass);
        countQuery.select(cb.count(countRoot));
        if (filter != null && !filter.isBlank()) {
            countQuery.where(PredicateBuilder.buildPredicate(filter, cb, countRoot));
        }
        return this.entityManager.createQuery(countQuery);
    }

    private <T> TypedQuery<T> getQuery(String filter, Pageable pageable, Class<T> domainClass) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = builder.createQuery(domainClass);
        Root<T> root = query.from(domainClass);
        if (filter != null && !filter.isBlank()) {
            query.where(PredicateBuilder.buildPredicate(filter, builder, root));
        }

        query.select(root);
        Sort sort = pageable.getSort();
        if (sort.isSorted()) {
            query.orderBy(QueryUtils.toOrders(sort, root, builder));
        }
        return entityManager.createQuery(query);
    }

}
