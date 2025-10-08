package cn.sparrowmini.common.repository;

import cn.sparrowmini.common.antlr.PredicateBuilder;
import cn.sparrowmini.common.util.JsonUtils;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.reflections.ReflectionUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.jpa.support.PageableUtils;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.util.*;

import static cn.sparrowmini.common.repository.ProjectionHelper.keyById;
import static cn.sparrowmini.common.repository.ProjectionHelper.projectCollectionV2;

@Slf4j
public class BaseRepositoryImpl<T, ID>
        extends SimpleJpaRepository<T, ID>
        implements BaseRepository<T, ID> {

    private final EntityManager em;
    private final Class<T> domainClass;
    private final JpaEntityInformation<T, ID> entityInformation;
    private final ProjectionFactory projectionFactory;

    public BaseRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager em, ProjectionFactory projectionFactory) {
        super(entityInformation, em);
        this.domainClass = entityInformation.getJavaType();
        this.em = em;
        this.entityInformation = (JpaEntityInformation<T, ID>) entityInformation;
        this.projectionFactory = projectionFactory;
    }

    @Override
    public <S> Optional<S> findByIdProjection(ID id, Class<S> projectionClass) {
        String idField = idFieldName();
        Specification<T> spec = (root, query, cb) -> cb.equal(root.get(idField), id);
        return findByProjection(PageRequest.of(0,1),spec,projectionClass).stream().findFirst();
//        return findBy(spec, q -> q.as(projectionClass).first());
    }


    @NonNull
    @Override
    public Class<T> domainType() {
        return this.domainClass;
    }

    @Override
    @NonNull
    public Class<ID> idType() {
        return entityInformation.getIdType();
    }

    @Override
    @NonNull
    public String idFieldName() {
        return entityInformation.getIdAttributeNames()
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No @Id field found"));
    }

    @Override
    @NonNull
    public Field idField() {
        final Class<T> entityClass = domainType();
        for (Field f : ReflectionUtils.getAllFields(entityClass)) {
            if (f.isAnnotationPresent(Id.class) || f.isAnnotationPresent(EmbeddedId.class)) {
                return f;
            }
        }
        throw new RuntimeException("找不到ID字段: " + entityClass.getName());
    }

    @Override
    public <P> Page<P> findByProjection(Pageable pageable, Specification<T> spec, Class<P> projectionClass) {
        return findByProjectionInternal(pageable, spec, null, projectionClass);
    }


    @Override
    public <P> Page<P> findByProjection(Pageable pageable, Predicate predicate, Class<P> projectionClass) {
        return findByProjectionInternal(pageable, null, predicate, projectionClass);
    }

    @Override
    public <P> Page<P> findByProjection(Pageable pageable, String filter, Class<P> projectionClass) {
        return findByProjectionInternal(pageable, null, null, projectionClass, filter);
    }

    /**
     * 内部统一方法，处理 Specification / Predicate / filter
     */
    private <P> Page<P> findByProjectionInternal(Pageable pageable,
                                                 Specification<T> spec,
                                                 Predicate predicate,
                                                 Class<P> projectionClass) {
        return findByProjectionInternal(pageable, spec, predicate, projectionClass, null);
    }

    /**
     * 主方法：分页查询 + 动态 DTO 投影 + 集合递归
     */
    private <P> Page<P> findByProjectionInternal(Pageable pageable,
                                                 Specification<T> spec,
                                                 Predicate predicate,
                                                 Class<P> projectionClass,
                                                 String filter) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<T> root = query.from(domainType());

        // 构建 Predicate
        Predicate finalPredicate = predicate != null ? predicate
                : spec != null ? spec.toPredicate(root, query, cb)
                : filter != null ? PredicateBuilder.buildPredicate(filter, cb, root)
                : cb.conjunction();

        // 构建 select 投影字段
        List<Selection<?>> selections = ProjectionHelper.buildEntitySelection(
                root, domainClass,projectionClass);
        query.multiselect(selections);
        query.where(finalPredicate);

        // 排序
        Sort sort = pageable.getSort();
        if (sort.isSorted()) {
            query.orderBy(QueryUtils.toOrders(sort, root, cb));
        }

        TypedQuery<Tuple> typedQuery = em.createQuery(query);
        if (pageable.isPaged()) {
            typedQuery.setFirstResult(PageableUtils.getOffsetAsInteger(pageable));
            typedQuery.setMaxResults(pageable.getPageSize());
        }

        List<Tuple> tuples = typedQuery.getResultList();
        final List<P> finalResult = new ArrayList<>();
        // 转换成 DTO，支持嵌套对象，这个为主表的对象，不含子集合的查询，但是对于非集合，则直接join出来了
        List<Map<String,Object>> results = ProjectionHelperUtil.tuplesToMap(tuples);


        //递归处理含有子集合的数据
        if(!projectionClass.isInterface()){

            Map<String, Field> entityFieldsMap = ProjectionHelperUtil.getDomainFieldsMap(domainClass);
            Map<String, Field> projectFieldsMap = ProjectionHelperUtil.getDomainFieldsMap(projectionClass);

            Collection<Field> entityFields = entityFieldsMap.values();
            Collection<Field> projectFields = projectFieldsMap.values();
            final Map<Object, Map<String, Object>> rootEntitiesByIdMap = keyById(results,domainClass);
            for(Field projectField: projectionClass.getDeclaredFields()){
                String projectFieldName = projectField.getName();
                Class<?> projectFieldClass = projectField.getType();
                Field entityField = entityFieldsMap.get(projectFieldName);
                if(entityField==null) continue;
                if(ProjectionHelperUtil.isCollectionField(projectFieldClass)){
                    //并且字段得是实体类型
                    Class<?> collectionEntityClass = ProjectionHelperUtil.getCollectionGenericClass(entityField);
                    Class<?> collectionProjectClass = ProjectionHelperUtil.getCollectionGenericClass(projectField);
                    if(ProjectionHelperUtil.isJavaStandardType(collectionProjectClass)){
                        continue;
                    }
                    log.debug("递归子集合1 字段名 {} 字段类型 {} 投影类型 {}, {}", projectFieldName, domainClass.getName(), collectionEntityClass.getName(), collectionProjectClass.getName());
                    projectCollectionV2(rootEntitiesByIdMap, domainClass ,collectionEntityClass, collectionProjectClass,em, projectFieldName);
                    try {
                        log.debug("回写后情况1 {} ",JsonUtils.getMapper().writeValueAsString(rootEntitiesByIdMap));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }

                if(ProjectionHelperUtil.isAssociationOne(entityField)){
                    log.debug("递归关联实体1 {}", projectFieldName);
                    Class<?> toOneEntityClass = entityField.getType();
                    final List<Map<String, Object>> childEntities = ProjectionHelper.getAllByKey(results,projectFieldName);
                    final Map<Object, Map<String, Object>> childEntitiesByIdMap = keyById(childEntities,toOneEntityClass);
                    projectCollectionV2(childEntitiesByIdMap, domainClass ,toOneEntityClass, projectFieldClass,em, projectFieldName);
                }
            }

            ObjectMapper mapper = JsonUtils.getMapper();
            JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, projectionClass);
            List<P> list = mapper.convertValue(results, type);
            finalResult.addAll(list);
        }


        // Count 查询
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<T> countRoot = countQuery.from(domainType());
        Predicate countPredicate = spec != null ? spec.toPredicate(countRoot, countQuery, cb)
                : filter != null ? PredicateBuilder.buildPredicate(filter, cb, countRoot)
                : predicate != null ? predicate
                : cb.conjunction();
        countQuery.select(cb.count(countRoot));
        countQuery.where(countPredicate);
        TypedQuery<Long> countTypedQuery = em.createQuery(countQuery);

        return PageableExecutionUtils.getPage(finalResult, pageable, countTypedQuery::getSingleResult);
    }


    @Override
    @Transactional
    public List<ID> upsert(List<Map<String, Object>> entitiesMap) {
        ObjectMapper mapper = JsonUtils.getMapper();
        // 允许 null 覆盖
        mapper.setDefaultSetterInfo(JsonSetter.Value.forValueNulls(Nulls.SET));
        List<T> entities = new ArrayList<>();

        entitiesMap.forEach(entityMap -> {
            String idFieldName = idFieldName();
            Object idRaw = entityMap.get(idFieldName);
            Class<ID> idClass = idType();
            T entity;

            if (idRaw != null && !idRaw.toString().isBlank()) {
                ID id = mapper.convertValue(idRaw, idClass);
                if (existsById(id)) {
                    Map<String, Object> patchCopy = new HashMap<>(entityMap);
                    patchCopy.remove(idFieldName);
                    entity = getReferenceById(id);
                    try {
                        mapper.updateValue(entity, patchCopy);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Patch更新失败 " + e.getOriginalMessage(), e);
                    }
                } else {
                    //不存在
                    entity = mapper.convertValue(entityMap, domainType());
                    //如果id不是自动生成的，则需要手动设置id
                    Field idField = idField();
                    if (!idField.isAnnotationPresent(GeneratedValue.class)) {
                        try {
                            PropertyDescriptor pd = new PropertyDescriptor(idField.getName(), entity.getClass());
                            Method setter = pd.getWriteMethod();
                            if (setter != null) {
                                setter.invoke(entity, id);
                            }
                        } catch (IllegalAccessException | IntrospectionException | InvocationTargetException e) {
                            throw new RuntimeException("无法设置ID字段", e);
                        }
                    }
                }
            } else {
                entity = mapper.convertValue(entityMap, domainType());
            }

            // 🔑 递归处理关联
            handleRelations(entity, entityMap, mapper);

            entities.add(entity);
        });

        saveAll(entities);
        return entities.stream().map(this::getId).toList();
    }



    /**
     * 递归处理关联字段
     */
    private void handleRelations(Object entity, Map<String, Object> entityMap, ObjectMapper mapper) {
        for (Field field : entity.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(OneToMany.class)) {
                Object raw = entityMap.get(field.getName());
                if (raw instanceof Collection<?> rawChildren) {
                    ParameterizedType genericType = (ParameterizedType) field.getGenericType();
                    Class<?> childType = (Class<?>) genericType.getActualTypeArguments()[0];

                    Collection<Object> children = new ArrayList<>();
                    for (Object childMap : rawChildren) {
                        if (!(childMap instanceof Map)) continue;
                        Object child = mapper.convertValue(childMap, childType);

                        // 设置反向关系
                        OneToMany oneToMany = field.getAnnotation(OneToMany.class);
                        if (!oneToMany.mappedBy().isEmpty()) {
                            try {
                                Field backRef = childType.getDeclaredField(oneToMany.mappedBy());
                                backRef.setAccessible(true);
                                backRef.set(child, entity);
                            } catch (NoSuchFieldException | IllegalAccessException e) {
                                throw new RuntimeException("设置反向关联失败", e);
                            }
                        }
                        children.add(child);
                    }

                    try {
                        field.setAccessible(true);
                        if (Set.class.isAssignableFrom(field.getType())) {
                            field.set(entity, new HashSet<>(children));
                        } else {
                            field.set(entity, children);
                        }
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else if (field.isAnnotationPresent(OneToOne.class) || field.isAnnotationPresent(ManyToOne.class)) {
                Object raw = entityMap.get(field.getName());
                if (raw instanceof Map<?, ?> rawChild) {
                    Class<?> childType = field.getType();
                    Object child = mapper.convertValue(rawChild, childType);

                    try {
                        field.setAccessible(true);
                        field.set(entity, child);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    protected Map<ID, T> getReferenceBy(Specification<T> spec) {
        Map<ID, T> refs = new HashMap<>();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ID> cq = cb.createQuery(idType());
        Root<T> root = cq.from(domainClass);

        cq.select(root.get(idFieldName()));
        cq.where(spec.toPredicate(root, cq, cb));

        List<ID> ids = em.createQuery(cq).getResultList();
        ids.forEach(id->refs.put(id,em.getReference(domainClass, id)));
        return refs;
    }

}