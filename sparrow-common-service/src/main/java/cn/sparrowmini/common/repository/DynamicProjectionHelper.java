package cn.sparrowmini.common.repository;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.data.jpa.support.PageableUtils;
import org.springframework.data.support.PageableExecutionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import cn.sparrowmini.common.antlr.PredicateBuilder;
import cn.sparrowmini.common.util.JsonUtils;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Tuple;
import jakarta.persistence.TupleElement;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;

/**
 * 动态构建 JPA Tuple 查询，并将结果自动映射为 Projection DTO。
 * 支持嵌套对象和集合（Collection）字段自动加载。
 * 主键总是基于 domain class，不依赖 Projection DTO。
 */
public class DynamicProjectionHelper {

    public static <P> Page<P> findAllProjection(EntityManager em, Class<?> domainType,
                                                Pageable pageable, String filter, Class<P> projectionClass) {

        // ----------------------------
        // Step 1: 构建查询并获取 Tuple
        // ----------------------------
        TypedQuery<Tuple> query = buildQuery(em, domainType, pageable, filter, projectionClass);
        if (pageable.isPaged()) {
            query.setFirstResult(PageableUtils.getOffsetAsInteger(pageable));
            query.setMaxResults(pageable.getPageSize());
        }
        List<Tuple> tuples = query.getResultList();
        ObjectMapper objectMapper = JsonUtils.getMapper();

        // ----------------------------
        // Step 2: Tuple -> List<Map<String,Object>>
        // ----------------------------
        List<Map<String, Object>> flatList = new ArrayList<>();
        Field domainIdField = findIdField(domainType);
        String domainIdName = domainIdField.getName();
        for (Tuple tuple : tuples) {
            Map<String, Object> map = new LinkedHashMap<>();
            for (TupleElement<?> elem : tuple.getElements()) {
                map.put(elem.getAlias(), tuple.get(elem));
            }
            flatList.add(map);
        }

        boolean hasCollectionField = Arrays.stream(projectionClass.getDeclaredFields())
                .anyMatch(f -> Collection.class.isAssignableFrom(f.getType()));

        if (hasCollectionField) {
        	// ----------------------------
            // Step 3: 加载集合字段
            // ----------------------------
            loadCollections(flatList, domainType, projectionClass, em, objectMapper, domainIdField);
        }

        

        // ----------------------------
        // Step 4: 最终转换为 DTO
        // ----------------------------
        List<P> result = new ArrayList<>();
        for (Map<String, Object> m : flatList) {
            P dto = objectMapper.convertValue(m, projectionClass);
            result.add(dto);
        }

        return PageableExecutionUtils.getPage(result, pageable,
                () -> getCountQuery(filter, domainType, em).getSingleResult());
    }

    
    
    // ----------------------------
    // 构建 Tuple 查询
    // ----------------------------
    public static <P> TypedQuery<Tuple> buildQuery(EntityManager em, Class<?> domainType, Pageable pageable,
                                                   String filter, Class<P> projectionClass) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<?> root = query.from(domainType);

        List<Selection<?>> selections = buildSelections(domainType, cb, root, projectionClass, "", new HashMap<>());
        query.multiselect(selections);

        if (filter != null && !filter.isBlank()) {
            query.where(PredicateBuilder.buildPredicate(filter, cb, root));
        }

        Sort sort = pageable.getSort();
        if (sort.isSorted()) {
            query.orderBy(QueryUtils.toOrders(sort, root, cb));
        }

        return em.createQuery(query);
    }

    public static <T> TypedQuery<Long> getCountQuery(String filter, Class<T> domainClass, EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<?> countRoot = countQuery.from(domainClass);
        countQuery.select(cb.count(countRoot));
        if (filter != null && !filter.isBlank()) {
            countQuery.where(PredicateBuilder.buildPredicate(filter, cb, countRoot));
        }
        return em.createQuery(countQuery);
    }

    // ----------------------------
    // 构建 select 字段
    // ----------------------------
    public static List<Selection<?>> buildSelections(Class<?> domainType, CriteriaBuilder cb, From<?, ?> root,
                                                     Class<?> projectionClass, String prefix, Map<String, From<?, ?>> joins) {
        List<Selection<?>> selections = new ArrayList<>();

        Field idField = findIdField(domainType);
        String idName = idField.getName();
        String alias = prefix.isEmpty() ? idName : prefix + "." + idName;
        selections.add(root.get(idName).alias(alias));

        for (Field field : projectionClass.getDeclaredFields()) {
            String fieldName = field.getName();
            Class<?> fieldType = field.getType();
            String fieldAlias = prefix.isEmpty() ? fieldName : prefix + "." + fieldName;

            if (Collection.class.isAssignableFrom(fieldType)) continue;
            if (isJavaStandardType(fieldType)) {
                selections.add(root.get(fieldName).alias(fieldAlias));
            } else {
                String joinPath = prefix.isEmpty() ? fieldName : prefix + "." + fieldName;
                From<?, ?> join = joins.computeIfAbsent(joinPath, k -> root.join(fieldName, JoinType.LEFT));
                selections.addAll(buildSelections(domainType, cb, join, fieldType, fieldAlias, joins));
            }
        }
        return selections;
    }

    // ----------------------------
    // 工具方法
    // ----------------------------
    private static Field findIdField(Class<?> type) {
        Class<?> current = type;
        while (current != null && !current.equals(Object.class)) {
            for (Field f : current.getDeclaredFields()) {
                if (f.isAnnotationPresent(Id.class) || f.isAnnotationPresent(EmbeddedId.class)) {
                    f.setAccessible(true);
                    return f;
                }
            }
            current = current.getSuperclass();
        }
        throw new IllegalArgumentException("No @Id/@EmbeddedId found in " + type.getName());
    }

    private static void loadCollections(List<Map<String, Object>> flatList, Class<?> domainClass,
                                        Class<?> projectionClass, EntityManager em, ObjectMapper objectMapper,
                                        Field domainIdField) {

        String domainIdName = domainIdField.getName();
        List<Object> ids = new ArrayList<>();
        Map<Object, Map<String, Object>> idToMap = new LinkedHashMap<>();

        for (Map<String, Object> m : flatList) {
            Object id = m.get(domainIdName);
            ids.add(id);
            idToMap.put(id, m);
        }

        for (Field collectionField : projectionClass.getDeclaredFields()) {
            if (!Collection.class.isAssignableFrom(collectionField.getType())) continue;

            Class<?> dtoChildType = extractGenericType(collectionField);

            Field domainCollectionField = Arrays.stream(domainClass.getDeclaredFields())
                    .filter(f -> f.getName().equals(collectionField.getName()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Cannot find domain collection field for " + collectionField.getName()));

            Class<?> entityChildType = (Class<?>) ((ParameterizedType) domainCollectionField.getGenericType())
                    .getActualTypeArguments()[0];

            Field manyToOneField = findManyToOneField(entityChildType, domainClass);
            String parentIdName = domainIdName;

            // 子表查询
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Tuple> cq = cb.createTupleQuery();
            Root<?> childRoot = cq.from(entityChildType);

            cq.multiselect(
                    childRoot,
                    childRoot.get(manyToOneField.getName()).get(parentIdName).alias(parentIdName)
            );
            cq.where(childRoot.get(manyToOneField.getName()).get(parentIdName).in(ids));

            List<Tuple> childTuples = em.createQuery(cq).getResultList();

            Map<Object, List<Map<String, Object>>> grouped = new LinkedHashMap<>();
            for (Tuple t : childTuples) {
                Object parentId = t.get(parentIdName);
                Map<String, Object> childMap = objectMapper.convertValue(t.get(0), Map.class);
                grouped.computeIfAbsent(parentId, k -> new ArrayList<>()).add(childMap);
            }

            // 填充到父 Map
            for (Map.Entry<Object, Map<String, Object>> entry : idToMap.entrySet()) {
                Object id = entry.getKey();
                Map<String, Object> parentMap = entry.getValue();
                List<Map<String, Object>> children = grouped.getOrDefault(id, List.of());
                parentMap.put(collectionField.getName(), children);
            }
        }
    }

    private static Field findManyToOneField(Class<?> childType, Class<?> parentType) {
        for (Field f : childType.getDeclaredFields()) {
            if (f.isAnnotationPresent(ManyToOne.class) && f.getType().equals(parentType)) {
                f.setAccessible(true);
                return f;
            }
        }
        throw new IllegalArgumentException(
                "Cannot find ManyToOne field in " + childType.getName() + " pointing to " + parentType.getName());
    }

    private static Class<?> extractGenericType(Field field) {
        Type type = field.getGenericType();
        if (type instanceof ParameterizedType pt) {
            Type actualType = pt.getActualTypeArguments()[0];
            if (actualType instanceof Class<?> clazz) return clazz;
        }
        throw new IllegalArgumentException("Cannot extract generic type for field: " + field.getName());
    }

    private static boolean isJavaStandardType(Class<?> clazz) {
        final Set<Class<?>> JAVA_TIME_TYPES = Set.of(
                java.time.LocalDate.class, java.time.LocalDateTime.class,
                java.time.OffsetDateTime.class, java.time.Instant.class,
                java.time.ZonedDateTime.class, java.time.OffsetTime.class,
                java.time.LocalTime.class, java.time.Duration.class,
                java.time.Period.class
        );

        return clazz.isPrimitive() ||
                clazz.getName().startsWith("java.lang.") ||
                clazz.equals(String.class) ||
                Number.class.isAssignableFrom(clazz) ||
                Date.class.isAssignableFrom(clazz) ||
                clazz.isEnum() ||
                JAVA_TIME_TYPES.contains(clazz);
    }
}
