package cn.sparrowmini.common.repository;

import cn.sparrowmini.common.util.JsonUtils;
import jakarta.persistence.*;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 辅助函数
 */
@Slf4j
public class ProjectionHelperUtil {


    /**
     * 缓存所有实体类的字段
     */
    public static final Map<Class<?>, Map<String, Field>> domainFields = new ConcurrentHashMap<>();

    /**
     * 缓存所有投影类的字段
     */
    public static final Map<Class<?>, Map<String, Field>> projectFields = new ConcurrentHashMap<>();


    public static Map<String, Field> getDomainFieldsMap(Class<?> clazz) {
        domainFields.computeIfAbsent(clazz, k -> {
            Map<String, Field> map = new HashMap<>();
            ReflectionUtils.doWithFields(clazz, field -> {
                int mods = field.getModifiers();

                // 排除 static、transient
                if (Modifier.isStatic(mods) || Modifier.isTransient(mods)) return;

                // 排除 JPA Transient
                if (field.isAnnotationPresent(Transient.class)) return;

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

    public static Field getDomainField(Class<?> domainClass, String fieldName) {
        return getDomainFieldsMap(domainClass).get(fieldName);
    }

    public static Map<String, Field> getProjectFieldsMap(Class<?> clazz) {
//        if(! clazz.isAnnotationPresent(Value.class)) return Map.of();
        //只接收@Value的DTO
        projectFields.computeIfAbsent(clazz, k -> {
            Map<String, Field> map = new HashMap<>();
            ReflectionUtils.doWithFields(clazz, field -> {
                map.put(field.getName(), field);
            });
            return map;
        });
        return projectFields.get(clazz);
    }

    public static Field getProjectField(Class<?> domainClass, String fieldName) {
        return getProjectFieldsMap(domainClass).get(fieldName);
    }

    public static Join<?, ?> tryGetOrCreateJoin(From<?, ?> from, String fieldName) {
        // 避免重复 join
        for (Join<?, ?> join : from.getJoins()) {
            if (join.getAttribute().getName().equals(fieldName)) {
                return join;
            }
        }
        return from.join(fieldName, JoinType.LEFT);
    }


    public static List<Map<String, Object>> tuplesToMap(List<Tuple> tuples){
        return tuples.stream()
                .map(tuple -> {
                    Map<String, Object> tupleMap = new HashMap<>();
                    Map<String, Map<String, Object>> nestedMaps = new HashMap<>();

                    for (TupleElement<?> elem : tuple.getElements()) {
                        String alias = elem.getAlias();
                        Object value = tuple.get(elem);

                        if (alias.contains(".")) {
                            String[] parts = alias.split("\\.", 2);
                            nestedMaps.computeIfAbsent(parts[0], k -> new HashMap<>())
                                    .put(parts[1], value);
                        } else {
                            tupleMap.put(alias, value);
                        }
                    }

                    nestedMaps.forEach(tupleMap::put);
                    return tupleMap;
                })
                .collect(Collectors.toList());
    }

    public static boolean hasNestedCollections(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
                .anyMatch(f -> Collection.class.isAssignableFrom(f.getType()));
    }



    public static Field findIdField(Class<?> type) {
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

    public static Field getField(Class<?> type, String name) {
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
    public static boolean isValidField(Field field) {
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

    /**
     * 非集合的关联关系
     * @param f
     * @return
     */
    public static boolean isAssociationOne(Field f) {
        return f.isAnnotationPresent(ManyToOne.class)
                || f.isAnnotationPresent(OneToOne.class);
    }

    public static boolean isEmbedded(Field f) {
        return f.isAnnotationPresent(Embedded.class)
                || f.isAnnotationPresent(EmbeddedId.class)
                || f.getType().isAnnotationPresent(Embeddable.class);
    }



    public static boolean isEmbeddedId(Field f) {
        return f.isAnnotationPresent(EmbeddedId.class)
                || f.getType().isAnnotationPresent(Embeddable.class);
    }

    public static String makeAlias(String prefix, String name) {
        return prefix == null || prefix.isEmpty() ? name : prefix + "." + name;
    }

    public static Class<?> extractGenericType(Field field) {
        Type type = field.getGenericType();
        if (type instanceof ParameterizedType pt) {
            Type actualType = pt.getActualTypeArguments()[0];
            if (actualType instanceof Class<?> clazz)
                return clazz;
        }
        throw new IllegalArgumentException("Cannot extract generic type for field: " + field.getName());
    }

    public static boolean isJavaStandardType(Class<?> clazz) {
        final Set<Class<?>> JAVA_TIME_TYPES = Set.of(java.time.LocalDate.class, java.time.LocalDateTime.class,
                java.time.OffsetDateTime.class, java.time.Instant.class, java.time.ZonedDateTime.class,
                java.time.OffsetTime.class, java.time.LocalTime.class, java.time.Duration.class,
                java.time.Period.class);

        return clazz.isPrimitive() || clazz.getName().startsWith("java.lang.") || clazz.equals(String.class)
                || Number.class.isAssignableFrom(clazz) || Date.class.isAssignableFrom(clazz) || clazz.isEnum()
                || JAVA_TIME_TYPES.contains(clazz);
    }

    public static boolean isJavaStandardType(Field field) {
        Class<?> clazz = field.getType();
        return isJavaStandardType(clazz) || field.isAnnotationPresent(Embedded.class);
    }

    /**
     * 动态获取集合元素 DTO 类
     */
    public static Class<?> getCollectionElementProjectionClass(Field projField, Class<?> elementType) {
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
    public static Class<?> getCollectionGenericClass(Field field) {
        try {
            return (Class<?>) ((java.lang.reflect.ParameterizedType) field.getGenericType())
                    .getActualTypeArguments()[0];
        } catch (Exception e) {
            throw new IllegalStateException("无法获取集合泛型类型: " + field.getName(), e);
        }
    }

    public static boolean isCollectionField(Class<?> clazz) {
        return Collection.class.isAssignableFrom(clazz);
    }





    public static boolean equals(Object obj1, Object obj2) {
        if (obj1 == obj2) return true;
        if (obj1 == null || obj2 == null) return false;
        if (!obj1.getClass().equals(obj2.getClass())) return false;

        Field[] fields = obj1.getClass().getDeclaredFields();

        for (Field field : fields) {
            log.info("field {} class {}", field.getName(), obj1.getClass().getName());
            // 跳过静态字段
            if (Modifier.isStatic(field.getModifiers())) continue;
            // 避免尝试访问 JDK 内部类的字段
            ReflectionUtils.makeAccessible(field);
            Object v1 = ReflectionUtils.getField(field, obj1);
            Object v2 = ReflectionUtils.getField(field, obj2);
            if (!Objects.equals(v1, v2)) {
                return false;
            }

        }

        return false;
    }

    public static Object buildIdValue(Object id, Class<?> idClass) {
        return JsonUtils.getMapper().convertValue(id, idClass);
    }



    public static Map<Object, List<Map<String, Object>>> groupByParentId(
            List<Tuple> childTuples,
            String parentRefField,
            List<Object> parentIds) {

        Map<Object, List<Map<String, Object>>> grouped = new LinkedHashMap<>();

        for (Tuple tuple : childTuples) {
            if (tuple == null) continue;
            Object parentId = extractParentId(tuple, parentRefField);
            if (parentId == null) continue;

            Map<String, Object> tupleMap = new HashMap<>();
            Map<String, Map<String, Object>> nestedMaps = new HashMap<>();

            for (TupleElement<?> elem : tuple.getElements()) {
                String alias = elem.getAlias();
                Object value = tuple.get(elem);

                if (alias.contains(".")) {
                    String[] parts = alias.split("\\.", 2);
                    nestedMaps.computeIfAbsent(parts[0], k -> new HashMap<>())
                            .put(parts[1], value);
                } else {
                    tupleMap.put(alias, value);
                }
            }

            nestedMaps.forEach(tupleMap::put);

//            Map<String, Object> map = new HashMap<>();
//                    tuple.getElements().forEach(element -> {
//                        map.put(element.getAlias(),tuple.get(element.getAlias()));
//                    });
            parentIds.stream().filter(f->isJavaStandardType(f.getClass())?f.equals(parentId): equals(f,parentId)).findFirst().ifPresent(parentId_->{
                grouped.computeIfAbsent(parentId_, k -> new ArrayList<>()).add(tupleMap);
            });

        }


        return grouped;
    }


    public static Class<?> getCollectionElementType(Field field) {
        ParameterizedType pt = (ParameterizedType) field.getGenericType();
        return (Class<?>) pt.getActualTypeArguments()[0];
    }

    public static String getParentReferenceField(Class<?> childType, Class<?> parentType) {
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

    public static Field getReferenceParentField(Class<?> childType, Class<?> parentType) {
        for (Field f : childType.getDeclaredFields()) {
            if (f.getType().equals(parentType)) {
                return f;
            }
        }
        return null;
    }



    public static Object extractParentId(Tuple tuple, String parentField) {
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


    public static boolean hasNestedCollections(Class<?> domainClass, Class<?> projectionClass) {
        for (Field projField : projectionClass.getDeclaredFields()) {
            if (!isValidField(projField)) continue;

            // 如果字段是集合
            if (isCollectionField(projField.getType())) {
                return true;
            }

            // 如果字段是普通对象，检查其内部集合字段
            Field domainField = getDomainField(domainClass, projField.getName());
            if (domainField != null && !isJavaStandardType(domainField.getType())) {
                Class<?> nestedProj = projField.getType();
                if (hasNestedCollections(domainField.getType(), nestedProj)) {
                    return true;
                }
            }
        }
        return false;
    }

}
