package cn.sparrowmini.common.repository;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import cn.sparrowmini.common.util.JpaUtils;
import cn.sparrowmini.common.util.JsonUtils;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.data.jpa.support.PageableUtils;
import org.springframework.data.support.PageableExecutionUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import cn.sparrowmini.common.antlr.PredicateBuilder;

/**
 * 动态构建 JPA Tuple 查询，并将结果自动映射为 Projection DTO。 支持嵌套对象和集合（Collection）字段自动加载。
 * 主键总是基于 domain class，不依赖 Projection DTO。
 */
@Slf4j
public class DynamicProjectionHelper {

	public static <P, ID> P findByIdProjection(EntityManager em, Class<?> domainType, ID id,
												Class<P> projectionClass) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Tuple> query = cb.createTupleQuery();
		Root<?> root = query.from(domainType);

		List<Selection<?>> selections = buildSelections(domainType, cb, root, projectionClass, "", new HashMap<>());
		query.multiselect(selections);
		Field idField = JpaUtils.getIdField(domainType);
		query.where(cb.equal(root.get(idField.getName()),id));
		TypedQuery<Tuple> typedQuery = em.createQuery(query);
		List<Tuple> tuples = typedQuery.getResultList();
		// ----------------------------
		// Step 2: Tuple -> List<Map<String,Object>>
		// ----------------------------
		final ObjectMapper objectMapper = JsonUtils.getMapper();
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

		try {
			loadSingle(flatList, domainType, projectionClass, em, objectMapper, domainIdField);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
		} ;
		return result.get(0);
	}

	public static <P> Page<P> findAllProjection(EntityManager em, Class<?> domainType, Pageable pageable, String filter,
			Class<P> projectionClass) {

		// ----------------------------
		// Step 1: 构建查询并获取 Tuple
		// ----------------------------
		TypedQuery<Tuple> query = buildQuery(em, domainType, pageable, filter, projectionClass);
		if (pageable.isPaged()) {
			query.setFirstResult(PageableUtils.getOffsetAsInteger(pageable));
			query.setMaxResults(pageable.getPageSize());
		}
		List<Tuple> tuples = query.getResultList();
		ObjectMapper objectMapper = new ObjectMapper();// 注册 Java 8 日期时间模块
		objectMapper.registerModule(new JavaTimeModule());
		        // 禁用时间戳输出（可选）
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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

		try {
			loadSingle(flatList, domainType, projectionClass, em, objectMapper, domainIdField);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
    public static List<Selection<?>> buildSelections(
            Class<?> entityType, CriteriaBuilder cb, From<?, ?> from,
            Class<?> projectionType, String prefix, Map<String, From<?, ?>> joins) {

        List<Selection<?>> selections = new ArrayList<>();

        // 仅当当前 from 对应的是实体时，才考虑添加它的 id
        Field idField = findIdField(entityType);
        String idName = idField.getName();
        String alias = prefix.isEmpty() ? idName : prefix + "." + idName;
        selections.add(from.get(idField.getName()).alias(alias));

        for (Field projField : projectionType.getDeclaredFields()) {
            String name = projField.getName();

            // 如果已经通过上面的 id 处理覆盖了，就跳过
            if (idField != null && name.equals(idField.getName())) {
                continue;
            }

            // 在实体上找同名的 domain 字段（为了知道它是关联、嵌入还是普通列）
            Field domainField = getField(entityType, name);
            Class<?> domainFieldType = (domainField != null) ? domainField.getType() : null;

            // 集合跳过（你的原逻辑）
            if (domainFieldType != null && Collection.class.isAssignableFrom(domainFieldType)) {
                continue;
            }

            String fieldAlias = makeAlias(prefix, name);

            if (domainField != null && isEmbedded(domainField) && !isAssociation(domainField)) {
                // 嵌入类型：不能 join，但JPA可以直接处理
                selections.add(from.get(name).alias(fieldAlias));
            } else if (domainField != null && isAssociation(domainField)) {
                // 关联实体：LEFT JOIN 然后递归（注意把 entityType 换成关联实体类型）
                String joinPath = makeAlias(prefix, name);
                From<?, ?> join = joins.computeIfAbsent(joinPath, k -> from.join(name, JoinType.LEFT));
                selections.addAll(buildSelections(
                        domainFieldType, cb, join,
                        projField.getType(), joinPath, joins));
            } else {
                // 普通标量字段，或实体里找不到对应字段但 projection 是标量 —— 直接取 get(name)
                if (isJavaStandardType(projField) || (domainField != null && isJavaStandardType(domainField))) {
                    selections.add(from.get(name).alias(fieldAlias));
                } else {
                    // 容错：如果既不是标量也不是已知关联/嵌入，避免误 join/误 get 带来异常
                    // 你也可以在这里记录一下日志便于排查
                    log.info("不处理的字段 {}", name);
                }
            }
        }

        return selections;
    }

    private static Field getField(Class<?> type, String name) {
        Class<?> t = type;
        while (t != null && t != Object.class) {
            try { Field f = t.getDeclaredField(name); f.setAccessible(true); return f; }
            catch (NoSuchFieldException ignored) {}
            t = t.getSuperclass();
        }
        return null;
    }

    private static boolean isAssociation(Field f) {
        return f.isAnnotationPresent(ManyToOne.class)
                || f.isAnnotationPresent(OneToOne.class);
    }
    private static boolean isEmbedded(Field f) {
        return f.isAnnotationPresent(Embedded.class)
                || f.isAnnotationPresent(EmbeddedId.class)
                || f.getType().isAnnotationPresent(Embeddable.class);
    }
    private static boolean isEmbeddedId(Field f) {
        return f.isAnnotationPresent(EmbeddedId.class)
                || f.getType().isAnnotationPresent(Embeddable.class);
    }
    private static String makeAlias(String prefix, String name) {
        return prefix == null || prefix.isEmpty() ? name : prefix + "." + name;
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

	// ---------------------------
	// 1️⃣ 单对象关联
	// ---------------------------
	private static void loadSingle(List<Map<String, Object>> flatList, Class<?> domainClass, Class<?> projectionClass,
			EntityManager em, ObjectMapper objectMapper, Field domainIdField) throws IllegalAccessException {

		if (flatList.isEmpty())
			return;

		Set<Object> parentIds = new HashSet<>();
		for (Map<String, Object> m : flatList) {
			Object id = m.get(domainIdField.getName());
			if (id != null)
				parentIds.add(id);
		}
		if (parentIds.isEmpty())
			return;

		for (Field field : projectionClass.getDeclaredFields()) {

			// 跳过集合字段和基础类型字段
			if (Collection.class.isAssignableFrom(field.getType()) || isJavaStandardType(field))
				continue;

			Field domainField = Arrays.stream(domainClass.getDeclaredFields())
					.filter(f -> f.getName().equals(field.getName())).findFirst().orElse(null);
			if (domainField == null)
				continue;
			if (!domainField.isAnnotationPresent(OneToOne.class) && !domainField.isAnnotationPresent(ManyToOne.class))
				continue;

			Class<?> childType = domainField.getType();
			Field childIdField = findIdField(childType);
			childIdField.setAccessible(true);

			List<Object> fkIds = flatList.stream().map(m -> m.get(domainField.getName() + "." + childIdField.getName()))
					.filter(Objects::nonNull).toList();
			if (fkIds.isEmpty())
				continue;

			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Tuple> cq = cb.createTupleQuery();
			Root<?> root = cq.from(childType);
			String childAlias = "child";

			cq.multiselect(root.alias(childAlias)).where(root.get(childIdField.getName()).in(fkIds));

			List<Tuple> childTuples = em.createQuery(cq).getResultList();

			Map<Object, Object> idToChild = new HashMap<>();
			for (Tuple t : childTuples) {
				Object childObj = t.get(childAlias);
				Object childId = childIdField.get(childObj);
				idToChild.put(childId, childObj);
			}

			for (Map<String, Object> m : flatList) {
				Object fkValue = m.get(domainField.getName() + "." + childIdField.getName());
				if (fkValue != null)
					m.put(field.getName(), idToChild.get(fkValue));
			}
		}
	}

	private static void loadCollections(List<Map<String, Object>> flatList, Class<?> domainClass,
			Class<?> projectionClass, EntityManager em, ObjectMapper objectMapper, Field domainIdField) {

		String domainIdName = domainIdField.getName();
		List<Object> ids = new ArrayList<>();
		Map<Object, Map<String, Object>> idToMap = new LinkedHashMap<>();

		for (Map<String, Object> m : flatList) {
			Object id = m.get(domainIdName);
			ids.add(id);
			idToMap.put(id, m);
		}

		for (Field collectionField : projectionClass.getDeclaredFields()) {

			if (!Collection.class.isAssignableFrom(collectionField.getType()))
				continue;

			Class<?> dtoChildType = extractGenericType(collectionField);
			Field domainCollectionField = Arrays.stream(domainClass.getDeclaredFields())
					.filter(f -> f.getName().equals(collectionField.getName())).findFirst()
					.orElseThrow(() -> new IllegalArgumentException(
							"Cannot find domain collection field for " + collectionField.getName()));

			Class<?> entityChildType = (Class<?>) ((ParameterizedType) domainCollectionField.getGenericType())
					.getActualTypeArguments()[0];


            // ----------------------------
            // 1) @ElementCollection
            // ----------------------------
            if (domainCollectionField.isAnnotationPresent(ElementCollection.class)) {
                CriteriaBuilder cb = em.getCriteriaBuilder();
                CriteriaQuery<Tuple> cq = cb.createTupleQuery();
                Root<?> parentRoot = cq.from(domainClass);
                Join<?, ?> childJoin = parentRoot.join(domainCollectionField.getName(), JoinType.LEFT);

                cq.multiselect(parentRoot.get(domainIdName).alias(domainIdName), childJoin);
                cq.where(parentRoot.get(domainIdName).in(ids));

                List<Tuple> rows = em.createQuery(cq).getResultList();
//                Map<Object, List<Map<String, Object>>> grouped = new LinkedHashMap<>();

                Map<Object, List<Object>> grouped = new LinkedHashMap<>();

                for (Tuple row : rows) {
                    Object parentId = row.get(domainIdName);
                    Object child = row.get(1); // 第二列是 childJoin
                    if (child != null) {
                        if(isJavaStandardType(entityChildType)){
                            grouped.computeIfAbsent(parentId, k -> new ArrayList<>()).add(child);
                        }else{
                            Map<String, Object> childMap = objectMapper.convertValue(child, Map.class);
                            grouped.computeIfAbsent(parentId, k -> new ArrayList<>()).add(childMap);
                        }

                    }
                }

                for (Map.Entry<Object, Map<String, Object>> entry : idToMap.entrySet()) {
                    entry.getValue().put(collectionField.getName(),
                            grouped.getOrDefault(entry.getKey(), List.of()));
                }
            }else {
                Field manyToOneField = findManyToOneField(entityChildType, domainClass);
                String parentIdName = domainIdName;

                // 子表查询
                CriteriaBuilder cb = em.getCriteriaBuilder();
                CriteriaQuery<Tuple> cq = cb.createTupleQuery();
                Root<?> childRoot = cq.from(entityChildType);

                cq.multiselect(childRoot, childRoot.get(manyToOneField.getName()).get(parentIdName).alias(parentIdName));
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
	}

	private static Field findManyToOneField(Class<?> childType, Class<?> parentType) {
        for (Field f : childType.getDeclaredFields()) {

            // 1️⃣ 原有 ManyToOne 检查
            if (f.isAnnotationPresent(ManyToOne.class) && f.getType().equals(parentType)) {
                f.setAccessible(true);
                return f;
            }
        }
		throw new IllegalArgumentException(
				"Cannot find ManyToOne field in " + childType.getName() + " pointing to " + parentType.getName());
	}

    // 辅助方法：获取集合泛型类型
    private static Class<?> getCollectionGenericType(Field field) {
        Type type = field.getGenericType();
        if (type instanceof ParameterizedType) {
            Type[] typeArgs = ((ParameterizedType) type).getActualTypeArguments();
            if (typeArgs.length == 1) {
                Type arg = typeArgs[0];
                if (arg instanceof Class<?>) {
                    return (Class<?>) arg;
                }
            }
        }
        return null;
    }

	private static Class<?> extractGenericType(Field field) {
		Type type = field.getGenericType();
		if (type instanceof ParameterizedType pt) {
			Type actualType = pt.getActualTypeArguments()[0];
			if (actualType instanceof Class<?> clazz)
				return clazz;
		}
		throw new IllegalArgumentException("Cannot extract generic type for field: " + field.getName());
	}

    private static boolean isJavaStandardType(Class<?> clazz) {
        final Set<Class<?>> JAVA_TIME_TYPES = Set.of(java.time.LocalDate.class, java.time.LocalDateTime.class,
                java.time.OffsetDateTime.class, java.time.Instant.class, java.time.ZonedDateTime.class,
                java.time.OffsetTime.class, java.time.LocalTime.class, java.time.Duration.class,
                java.time.Period.class);

        return clazz.isPrimitive() || clazz.getName().startsWith("java.lang.") || clazz.equals(String.class)
                || Number.class.isAssignableFrom(clazz) || Date.class.isAssignableFrom(clazz) || clazz.isEnum()
                || JAVA_TIME_TYPES.contains(clazz);
    }

	private static boolean isJavaStandardType(Field field) {
        Class<?> clazz = field.getType();
		return isJavaStandardType(clazz) || field.isAnnotationPresent(Embedded.class);
	}
}
