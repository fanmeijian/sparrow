//package cn.sparrowmini.common.repository;
//
//import java.lang.reflect.Field;
//import java.lang.reflect.Method;
//import java.lang.reflect.ParameterizedType;
//import java.lang.reflect.Type;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.data.jpa.repository.query.QueryUtils;
//import org.springframework.data.jpa.support.PageableUtils;
//import org.springframework.data.support.PageableExecutionUtils;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//import cn.sparrowmini.common.antlr.PredicateBuilder;
//import cn.sparrowmini.common.util.JsonUtils;
//import jakarta.persistence.EntityManager;
//import jakarta.persistence.Tuple;
//import jakarta.persistence.TupleElement;
//import jakarta.persistence.TypedQuery;
//import jakarta.persistence.criteria.CriteriaBuilder;
//import jakarta.persistence.criteria.CriteriaQuery;
//import jakarta.persistence.criteria.From;
//import jakarta.persistence.criteria.JoinType;
//import jakarta.persistence.criteria.Root;
//import jakarta.persistence.criteria.Selection;
//
//public class BaseRepositoryHelper {
//	
//	public static <P> Page<P> findAllProjection(EntityManager em, Class<?> domainType, Pageable pageable, String filter,
//			Class<P> projectionClass) {
//		TypedQuery<Tuple> query = buildQuery(em, domainType, pageable, filter, projectionClass);
//		if (pageable.isPaged()) {
//			query.setFirstResult(PageableUtils.getOffsetAsInteger(pageable));
//			query.setMaxResults(pageable.getPageSize());
//		}
//
//		List<Tuple> tuples = query.getResultList();
//
////		List<P> results = tuples.stream().map(tuple -> {
////			Map<String, Object> tupleMap = new HashMap<>();
////			for (TupleElement<?> elem : tuple.getElements()) {
////				tupleMap.put(elem.getAlias(), tuple.get(elem));
////			}
////			try {
////				System.out.println(JsonUtils.getMapper().writeValueAsString(tuple));
////			} catch (JsonProcessingException e) {
////				// TODO Auto-generated catch block
////				e.printStackTrace();
////			}
////			Map<String, Object> nestedMap = convertFlatToNestedMap(tupleMap);
////
////			return JsonUtils.getMapper().convertValue(nestedMap, projectionClass);
////		}).collect(Collectors.toList());
//		
//		// 1. 主查询
////		List<Tuple> tuples = em.createQuery(cq).getResultList();
//
//		// 2. 转换 root DTO（不包含集合）
//		ObjectMapper objectMapper = JsonUtils.getMapper();
//		List<P> result = DynamicProjectionHelper.mapTuplesToProjectionAuto(tuples, projectionClass, objectMapper);
//
//		// 3. 再补集合字段（子查询）
//		DynamicProjectionHelper.loadCollections(result, projectionClass, em, objectMapper);
//
//		return PageableExecutionUtils.getPage(result, pageable, () -> {
//			return getCountQuery(filter, domainType, em).getSingleResult();
//		});
//	}
//
//	public static Map<String, Object> convertFlatToNestedMap(Map<String, Object> flatMap) {
//	    Map<String, Object> nestedMap = new HashMap<>();
//
//	    for (Map.Entry<String, Object> entry : flatMap.entrySet()) {
//	        String[] keys = entry.getKey().split("\\.");
//	        Map<String, Object> current = nestedMap;
//
//	        for (int i = 0; i < keys.length - 1; i++) {
//	            String key = keys[i];
//	            Object nextLevel = current.get(key);
//
//	            if (!(nextLevel instanceof Map)) {
//	                nextLevel = new HashMap<String, Object>();
//	                current.put(key, nextLevel);
//	            }
//
//	            current = (Map<String, Object>) nextLevel;
//	        }
//
//	        current.put(keys[keys.length - 1], entry.getValue());
//	    }
//
//	    return nestedMap;
//	}
//
//	
//	public static <P> TypedQuery<Tuple> buildQuery(EntityManager em, Class<?> domainType, Pageable pageable,
//			String filter, Class<P> projectionClass) {
//		CriteriaBuilder cb = em.getCriteriaBuilder();
//		CriteriaQuery<Tuple> query = cb.createTupleQuery();
//		Root<?> root = query.from(domainType);
//
//		// 构建select投影字段
//		List<Selection<?>> selections = DynamicProjectionHelper.buildSelections(domainType, cb, root, projectionClass, "", new HashMap<String, From<?,?>>()); //buildSelections(domainType,cb, root, projectionClass, "", new HashMap<>());
//		query.multiselect(selections);
//
//		// 条件过滤
//		if (filter != null && !filter.isBlank()) {
//			query.where(PredicateBuilder.buildPredicate(filter, cb, root));
//		}
//
//		// 排序
//		Sort sort = pageable.getSort();
//		if (sort.isSorted()) {
//			query.orderBy(QueryUtils.toOrders(sort, root, cb));
//		}
//
//		return em.createQuery(query);
//	}
//
//	private static List<Selection<?>> buildSelections(Class<?> domainType, CriteriaBuilder cb, From<?, ?> root, Class<?> projectionClass,
//			String prefix, Map<String, From<?, ?>> joins) {
//
//		System.out.println(extractPropertyPaths(projectionClass, ""));
//		List<Selection<?>> selections = new ArrayList<>();
//		System.out.println("projectionClass = " + projectionClass);
//		System.out.println("isInterface = " + projectionClass.isInterface());
//
//		if (projectionClass.isInterface()) {
//			for (Method method : projectionClass.getMethods()) {
//				if (method.getParameterCount() != 0)
//					continue;
//				String methodName = method.getName();
//				if (!(methodName.startsWith("get") || methodName.startsWith("is")))
//					continue;
//
//				String propName = methodName.startsWith("get") ? methodName.substring(3) : methodName.substring(2);
//				propName = Character.toLowerCase(propName.charAt(0)) + propName.substring(1);
//
//				Class<?> returnType = method.getReturnType();
//
//				if (isJavaStandardType(returnType)) {
//					selections.add(root.get(prefix + propName).alias(prefix + propName));
//				} else {
//					// 递归处理嵌套对象
//					// 复杂类型需要join
//					String joinPath = prefix + propName;
//					From<?, ?> join = joins.get(joinPath);
//					if (join == null) {
//						join = root.join(propName, JoinType.LEFT);
//						joins.put(joinPath, join);
//					}
//					// 递归处理子属性，prefix清空因为join已经定位了路径
//					selections.addAll(buildSelections(domainType,cb, join, returnType, "", joins));
//				}
//			}
//		} else {
//			
//			for (Field field : projectionClass.getDeclaredFields()) {
//			    String fieldName = field.getName();
//			    Class<?> fieldType = field.getType();
//
//			    // 构建当前字段的 alias，确保没有重复的 "."
//			    String alias = prefix.isEmpty() ? fieldName : prefix + "." + fieldName;
//
//			    // 1. 判断是否是集合
//			    if (Collection.class.isAssignableFrom(fieldType)) {
//			        Type genericType = field.getGenericType();
//			        if (genericType instanceof ParameterizedType pt) {
//			            Type actualType = pt.getActualTypeArguments()[0];
//			            if (actualType instanceof Class<?> elementType) {
//			                String joinPath = prefix.isEmpty() ? fieldName : prefix + "." + fieldName;
//			                From<?, ?> join = joins.get(joinPath);
//			                if (join == null) {
//			                    join = root.join(fieldName, JoinType.LEFT);
//			                    joins.put(joinPath, join);
//			                }
//
//			                // ✅ 正确拼接下一级前缀，防止重复点号
//			                String nextPrefix = alias;
//			                selections.addAll(buildSelections(
//			                        elementType, cb, join, elementType, nextPrefix, joins));
//			            }
//			        }
//			        continue;
//			    }
//
//			    // 2. 简单字段
//			    if (isJavaStandardType(fieldType)) {
//			        selections.add(root.get(fieldName).alias(alias));
//			    } else {
//			        // 3. 嵌套对象字段
//			        String joinPath = prefix.isEmpty() ? fieldName : prefix + "." + fieldName;
//			        From<?, ?> join = joins.get(joinPath);
//			        if (join == null) {
//			            join = root.join(fieldName, JoinType.LEFT);
//			            joins.put(joinPath, join);
//			        }
//
//			        String nextPrefix = alias;
//			        selections.addAll(buildSelections(
//			                fieldType, cb, join, fieldType, nextPrefix, joins));
//			    }
//			}
//
//
//			
////			for (Field field : projectionClass.getDeclaredFields()) {
////				String fieldName = field.getName();
////				Class<?> fieldType = field.getType();
////				String alias = (prefix.isEmpty() ? "" : prefix + ".") + fieldName;
////				
////				// 1. 判断是否是集合
////				if (Collection.class.isAssignableFrom(fieldType)) {
////					// 提取集合的泛型类型
////					Type genericType = field.getGenericType();
////					if (genericType instanceof ParameterizedType pt) {
////						Type actualType = pt.getActualTypeArguments()[0];
////						if (actualType instanceof Class<?> elementType) {
////							// 建立 join（对集合必须是 joinList，否则 CriteriaBuilder 报错）
////							String joinPath = prefix + fieldName;
////							From<?, ?> join = joins.get(joinPath);
////							if (join == null) {
////								join = root.join(fieldName, JoinType.LEFT); // 对集合通常还是用 LEFT
////								joins.put(joinPath, join);
////							}
////
////							// 递归处理集合元素类型
////							List<Selection<?>> subSelections = buildSelections(elementType, cb, join, elementType, "", joins);
////							selections.addAll(subSelections);
////						}
////					}
////					continue;
////				}
////				
////				// 2. 简单类型字段，直接 select
////				if (isJavaStandardType(fieldType)) {
////					selections.add(root.get(fieldName).alias(alias));
////				} else {
////					// 3. 复杂类型字段（嵌套对象） -> join + 递归处理
////					String joinPath = prefix + fieldName;
////					From<?, ?> join = joins.get(joinPath);
////					if (join == null) {
////						join = root.join(fieldName, JoinType.LEFT);
////						joins.put(joinPath, join);
////					}
////					selections.addAll(buildSelections(fieldType, cb, join, fieldType, alias + ".", joins));
////
////
////				}
////			}
//		}
//
//		return selections;
//	}
//
//	
//	private static boolean isJavaStandardType(Class<?> clazz) {
//	    final Set<Class<?>> JAVA_TIME_TYPES = Set.of(
//	        java.time.LocalDate.class,
//	        java.time.LocalDateTime.class,
//	        java.time.OffsetDateTime.class,
//	        java.time.Instant.class,
//	        java.time.ZonedDateTime.class,
//	        java.time.OffsetTime.class,
//	        java.time.LocalTime.class,
//	        java.time.Duration.class,
//	        java.time.Period.class
//	    );
//
//	    return clazz.isPrimitive()
//	        || clazz.getName().startsWith("java.lang.")
//	        || clazz.equals(String.class)
//	        || Number.class.isAssignableFrom(clazz)
//	        || Date.class.isAssignableFrom(clazz)
//	        || clazz.isEnum()
//	        || JAVA_TIME_TYPES.contains(clazz);
//	}
//	
//	private static boolean isCollectionType(Class<?> clazz) {
//	    return Collection.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz);
//	}
//
//
//
//	private <T> TypedQuery<T> getQuery(String filter, Pageable pageable, Class<T> domainClass, EntityManager em) {
//		CriteriaBuilder builder = em.getCriteriaBuilder();
//		CriteriaQuery<T> query = builder.createQuery(domainClass);
//		Root<T> root = query.from(domainClass);
//		if (filter != null && !filter.isBlank()) {
//			query.where(PredicateBuilder.buildPredicate(filter, builder, root));
//		}
//
//		query.select(root);
//		Sort sort = pageable.getSort();
//		if (sort.isSorted()) {
//			query.orderBy(QueryUtils.toOrders(sort, root, builder));
//		}
//		return em.createQuery(query);
//	}
//
//	private static <T> TypedQuery<Long> getCountQuery(String filter, Class<T> domainClass, EntityManager em) {
//		CriteriaBuilder cb = em.getCriteriaBuilder();
//		CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
//		Root<?> countRoot = countQuery.from(domainClass);
//		countQuery.select(cb.count(countRoot));
//		if (filter != null && !filter.isBlank()) {
//			countQuery.where(PredicateBuilder.buildPredicate(filter, cb, countRoot));
//		}
//		return em.createQuery(countQuery);
//	}
//
//	private static String[] getPropertyNames(Class<?> projectionClass) {
//		List<String> paths = extractPropertyPaths(projectionClass, "");
//		System.out.println("Projection fields: " + Arrays.toString(paths.toArray(new String[0])));
//		return paths.toArray(new String[0]);
//	}
//
//	private static List<String> extractPropertyPaths(Class<?> projectionClass, String prefix) {
//		List<String> props = new ArrayList<>();
//
//		if (projectionClass.isInterface()) {
//			for (Method method : projectionClass.getMethods()) {
//				if (method.getParameterCount() != 0)
//					continue;
//				String methodName = method.getName();
//				if (!(methodName.startsWith("get") || methodName.startsWith("is")))
//					continue;
//
//				String propName = methodName.startsWith("get") ? methodName.substring(3) : methodName.substring(2);
//				propName = Character.toLowerCase(propName.charAt(0)) + propName.substring(1);
//
//				Class<?> returnType = method.getReturnType();
//
//				if (isJavaStandardType(returnType)) {
//					props.add(prefix + propName);
//				} else {
//					// 递归处理嵌套对象
//					props.addAll(extractPropertyPaths(returnType, prefix + propName + "."));
//				}
//			}
//		} else {
//			// DTO类，直接字段，不递归（如果你需要，也可以实现）
//			for (Field field : projectionClass.getDeclaredFields()) {
//				props.add(prefix + field.getName());
//			}
//		}
//
//		return props;
//	}
//
//}
