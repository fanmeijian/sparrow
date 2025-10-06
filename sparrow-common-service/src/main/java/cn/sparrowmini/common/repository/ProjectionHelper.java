package cn.sparrowmini.common.repository;


import cn.sparrowmini.common.util.JsonUtils;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 支持循环递归构建SELECTION
 */
@Slf4j
public class ProjectionHelper {




    /**
     * 缓存所有实体类的字段
     */
    private static final Map<Class<?>, Map<String, Field>> domainFields = new ConcurrentHashMap<>();


    private static Map<String, Field> getDomainFieldsMap(Class<?> clazz) {
        domainFields.computeIfAbsent(clazz, k -> {
            Map<String, Field> map = new HashMap<>();
//            ReflectionUtils.getAllFields(domainClass).forEach(field -> {
//                map.put(field.getName(), field);
//            });

            ReflectionUtils.doWithFields(clazz, field -> {
                int mods = field.getModifiers();

                // 排除 static、transient
                if (Modifier.isStatic(mods) || Modifier.isTransient(mods)) return;

                // 排除 JPA Transient
                if (field.isAnnotationPresent(Transient.class)) return;

                // 排除 Jackson 忽略字段
//                if (field.isAnnotationPresent(JsonIgnore.class) ||
//                        field.isAnnotationPresent(JsonBackReference.class)) return;

                // 如果字段不是 public，也不是通过注解标记的持久化字段，可以额外排除
                if (!Modifier.isPublic(mods)
                        && !field.isAnnotationPresent(Column.class)
                        && !field.isAnnotationPresent(JoinColumn.class)
                        && !field.isAnnotationPresent(OneToMany.class)
                        && !field.isAnnotationPresent(ManyToOne.class)
                        && !field.isAnnotationPresent(OneToOne.class)
                        && !field.isAnnotationPresent(Embedded.class)
                        && !field.isAnnotationPresent(ElementCollection.class)
                ) {
                    // 可选：排除没有 getter 且非 JPA 注解的字段
                    try {
                        String getterName = "get" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
                        clazz.getMethod(getterName);
                    } catch (NoSuchMethodException e) {
                        return;
                    }
                }
                map.put(field.getName(), field);
            });

            return map;
        });
        return domainFields.get(clazz);
    }

    private static Field getDomainField(Class<?> domainClass, String fieldName) {
        return getDomainFieldsMap(domainClass).get(fieldName);
    }

    public static Map<String, List<Selection<?>>> getSelections(Class<?> domainClass, Class<?> projectClass) {
        return Map.of();
    }

    /**
     * key为字段名，根为类名例如实体类为Order 里面有products集合字段，则根的KEY为Order,集合字段root.products
     *
     * @return
     */
    public static List<Selection<?>> buildSelections(
            String prefix,
            From<?, ?> from,
            CriteriaBuilder cb,
            Class<?> domainClass,
            Class<?> projectionClass) {

        List<Selection<?>> selections = new ArrayList<>();

        for (Field projField : projectionClass.getDeclaredFields()) {
            if (!isValidField(projField)) continue;
            if (isCollectionField(projField.getType())) continue;


            String projFieldName = projField.getName();
            Field domainField = getDomainField(domainClass, projFieldName);
            if (domainField == null) continue;

            String domainFieldName = domainField.getName();
            String alias = prefix.isEmpty() ? projFieldName : prefix + "." + projFieldName;

            // ---------------------------
            // 关联实体（递归处理）
            // ---------------------------
            if (isAssociation(domainField)) {
                Join<?, ?> join = tryGetOrCreateJoin(from, domainFieldName);
                Class<?> joinClass = domainField.getType();

                // 递归展开子类字段
//                if (domainField.isAnnotationPresent(JsonIgnore.class) ||
//                        domainField.isAnnotationPresent(JsonBackReference.class)) {
//                    continue; // 不递归
//                }
                selections.addAll(buildSelections(alias, join, cb, joinClass, projField.getType()));
            }
            // ---------------------------
            // 嵌入类型
            // ---------------------------
            else if (isEmbedded(domainField)) {
                Path<?> path = from.get(domainFieldName);
                for (Field subField : domainField.getType().getDeclaredFields()) {
                    if (!isValidField(subField)) continue;
                    String subAlias = alias + "." + subField.getName();
                    selections.add(path.get(subField.getName()).alias(subAlias));
                }
            }
            // ---------------------------
            // 标量字段
            // ---------------------------
            else if (isJavaStandardType(projField) || isJavaStandardType(domainField)) {
                selections.add(from.get(domainFieldName).alias(alias));
            } else {
                log.info("跳过不处理字段: {}", domainFieldName);
            }
        }

        return selections;
    }

    private static Join<?, ?> tryGetOrCreateJoin(From<?, ?> from, String fieldName) {
        // 避免重复 join
        for (Join<?, ?> join : from.getJoins()) {
            if (join.getAttribute().getName().equals(fieldName)) {
                return join;
            }
        }
        return from.join(fieldName, JoinType.LEFT);
    }





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

    private static Field getField(Class<?> type, String name) {
        Class<?> t = type;
        while (t != null && t != Object.class) {
            try {
                Field f = t.getDeclaredField(name);
                f.setAccessible(true);
                return f;
            } catch (NoSuchFieldException ignored) {
            }
            t = t.getSuperclass();
        }
        return null;
    }


    /**
     * 不为NULL，且不是TRANSIENT，也不是FINAL STATIC
     *
     * @param field
     * @return
     */
    private static boolean isValidField(Field field) {
        if (field == null) {
            return false;
        }

        if (field.isAnnotationPresent(Transient.class)) {
            return false;
        }


        int modifiers = field.getModifiers();
        if (Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)) {
            return false;
        }

        return true;
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

    /**
     * 动态获取集合元素 DTO 类
     */
    private static Class<?> getCollectionElementProjectionClass(Field projField, Class<?> elementType) {
        // 如果 DTO 的集合泛型是接口或具体类，直接返回
        if (projField.getGenericType() instanceof ParameterizedType pt) {
            Type[] args = pt.getActualTypeArguments();
            if (args.length == 1) {
                Type t = args[0];
                if (t instanceof Class<?> clazz) return clazz;
            }
        }
        return elementType;
    }

    /**
     * 获取集合字段的泛型
     */
    private static Class<?> getCollectionGenericClass(Field field) {
        try {
            return (Class<?>) ((java.lang.reflect.ParameterizedType) field.getGenericType())
                    .getActualTypeArguments()[0];
        } catch (Exception e) {
            throw new IllegalStateException("无法获取集合泛型类型: " + field.getName(), e);
        }
    }

    private static boolean isCollectionField(Class<?> clazz) {
        return Collection.class.isAssignableFrom(clazz);
    }




    public static void loadCollectionsV2(
            List<Map<String, Object>> parentResults,
            Class<?> domainClass,
            Class<?> projectionClass,
            EntityManager em,
            Field domainIdField) {

        for (Field projField : projectionClass.getDeclaredFields()) {
            if (!isCollectionField(projField.getType())) continue;

            String collectionName = projField.getName();
            Field domainField = getDomainField(domainClass, collectionName);
            if (domainField == null) continue;

            // 集合元素类型（如 OrderItem.class）
            Class<?> elementType = getCollectionElementType(domainField);
            Class<?> elementProjectType = getCollectionGenericClass(projField);

            // 外键字段（假设用 ManyToOne 映射回父类）
            String parentRefField = getParentReferenceField(elementType, domainClass);
            Field parentIdField=findIdField(domainClass);
            if (parentRefField == null) continue;

            // ---------------------------
            // Step 1: 收集父 ID 列表
            // ---------------------------

            parentResults.forEach(parentResult -> {
                parentResult.put(domainIdField.getName(),buildIdValue(parentResult.get(domainIdField.getName()),parentIdField.getType()));
            });

            List<Object> parentIds = parentResults.stream()
                    .map(m -> m.get(domainIdField.getName()))
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());


//            List<Object> parentIds = parentResults.stream()
//                    .map(m -> m.get(domainIdField.getName()))
//                    .filter(Objects::nonNull)
//                    .distinct()
//                    .collect(Collectors.toList());

            if (parentIds.isEmpty()) continue;

            // ---------------------------
            // Step 2: 查询子集合
            // ---------------------------
            List<Tuple> childTuples = fetchChildTuples(em, elementType, parentRefField, parentIds,elementProjectType);

            // ---------------------------
            // Step 3: 按父 ID 分组
            // ---------------------------
            Map<Object, List<Map<String, Object>>> grouped = groupByParentId(childTuples, parentRefField, parentIds);

            // ---------------------------
            // Step 4: 填充到父结果
            // ---------------------------
            for (Map<String, Object> parentMap : parentResults) {
                Object parentId = parentMap.get(domainIdField.getName());
//                List<Map<String, Object>> children = null;
//                try {
//                    children = grouped.getOrDefault(JsonUtils.getMapper().writeValueAsString(parentId), Collections.emptyList());
//                } catch (JsonProcessingException e) {
//                    throw new RuntimeException(e);
//                }

                List<Map<String, Object>> children = grouped.get(parentId);
                parentMap.put(collectionName, children);
            }

            // ---------------------------
            // Step 5: 递归处理子集合
            // ---------------------------
            if (hasNestedCollections(elementType)) {
                loadCollectionsV2(
                        grouped.values().stream().flatMap(List::stream).collect(Collectors.toList()),
                        elementType,
                        getCollectionElementType(projField),
                        em,
                        findIdField(elementType)
                );
            }
        }
    }

    private static boolean equals(Object obj1, Object obj2){
        Field[] fields1 = obj1.getClass().getDeclaredFields();
        List<Boolean> r = new ArrayList<>();
        for (Field field : fields1) {
            field.setAccessible(true);
            try {
//                log.info("ob1 k {} v {}", field.getName(), field.get(obj1));
//                log.info("ob2 k {} v {}", field.getName(), field.get(obj2));
//                log.info("ob2 k {} v {}", field.getName(), field.get(obj2));
                boolean fieldEqua = field.get(obj1).equals(field.get(obj2));
                if(fieldEqua){
                    log.info("fieldEqua k {} v1 {} v2{}", field.getName(), field.get(obj1), field.get(obj2));
                }
                r.add(fieldEqua);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        boolean result = r.stream().allMatch(a->a==true);
        if(result){
            log.info("result{} obj1 {} obj2 {}",result, obj1, obj2);
        }

        return result;
    }

    private static Object buildIdValue(Object id, Class<?> idClass) {
        return JsonUtils.getMapper().convertValue(id, idClass);
    }

    private static List<Tuple> fetchChildTuples(
            EntityManager em,
            Class<?> elementType,
            String parentRefField,
            List<Object> parentIds,
            Class<?> elementProjectType) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<?> root = cq.from(elementType);
        //符合组件
        Path<?> path = root;
        String[] segments = parentRefField.split("\\.");
        for (String s : segments) {
            path = path.get(s);
        }

        // 选择子类所有非集合字段
        List<Selection<?>> selections = buildSelections("", root, cb, elementType, elementProjectType);
        selections.add(path.alias(parentRefField));
        cq.multiselect(selections);
        final Path<?> path_=path;


        List<Predicate> predicates = new ArrayList<>();
        if(!parentIds.isEmpty() && !isJavaStandardType(parentIds.get(0).getClass())) {
            Class<?> parentIdType = parentIds.get(0).getClass();
            Field[] fields = parentIdType.getDeclaredFields();
            for (Object searchId : parentIds) {
                List<Predicate> andPredicates = new ArrayList<>();
                for (Field field : fields) {
                    field.setAccessible(true);
                    try {
                        andPredicates.add(cb.equal(path_.get(field.getName()), field.get(searchId)));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
                Predicate andGroup = cb.and(andPredicates.toArray(new Predicate[0]));
                predicates.add(andGroup);
            }

            cq.where(cb.or(predicates.toArray(new Predicate[0])));
        }else{
            cq.where(path_.in(parentIds));
        }

        return em.createQuery(cq).getResultList();
    }


    private static Map<Object, List<Map<String, Object>>> groupByParentId(
            List<Tuple> childTuples,
            String parentRefField,
            List<Object> parentIds) {

        Map<Object, List<Map<String, Object>>> grouped = new LinkedHashMap<>();

        for (Tuple t : childTuples) {
            if (t == null) continue;
            Object parentId = extractParentId(t, parentRefField);
            if (parentId == null) continue;

            Map<String, Object> map = new HashMap<>();
                    t.getElements().forEach(element -> {
                        map.put(element.getAlias(),t.get(element.getAlias()));
                    });
            parentIds.stream().filter(f->equals(f,parentId)).findFirst().ifPresent(child->{
                grouped.computeIfAbsent(child, k -> new ArrayList<>()).add(map);
            });

        }


        return grouped;
    }


    private static Class<?> getCollectionElementType(Field field) {
        ParameterizedType pt = (ParameterizedType) field.getGenericType();
        return (Class<?>) pt.getActualTypeArguments()[0];
    }

    private static String getParentReferenceField(Class<?> childType, Class<?> parentType) {
        for (Field f : childType.getDeclaredFields()) {
            if (f.getType().equals(parentType)) {
                Field idField = findIdField(parentType);
                return String.join(".",f.getName(), idField.getName());
//                if(f.isAnnotationPresent(JoinColumn.class)) {
//                    return f.getAnnotation(JoinColumn.class).name();
//                }
//                JoinColumns joinColumns = f.getAnnotation(JoinColumns.class);
//                if(joinColumns != null){
//                  List<String> columns= Arrays.stream(joinColumns.value()).map(JoinColumn::name).toList();
//                  return String.join(",", columns);
//                }
            }
        }
        return null;
    }



    private static Object extractParentId(Tuple tuple, String parentField) {
        if (tuple == null || parentField == null) return null;

        try {
            // 直接取别名
            Object value = tuple.get(parentField);
            if (value != null) return value;

            // 如果是嵌套形式，比如 parent.id
            if (parentField.contains(".")) {
                String[] parts = parentField.split("\\.");
                Object current = tuple.get(parts[0]);
                for (int i = 1; i < parts.length && current != null; i++) {
                    Field f = current.getClass().getDeclaredField(parts[i]);
                    f.setAccessible(true);
                    current = f.get(current);
                }
                return current;
            }
        } catch (Exception e) {
            // 这里不要抛异常，直接返回 null
            return null;
        }

        return null;
    }


    private static boolean hasNestedCollections(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
                .anyMatch(f -> Collection.class.isAssignableFrom(f.getType()));
    }


}
