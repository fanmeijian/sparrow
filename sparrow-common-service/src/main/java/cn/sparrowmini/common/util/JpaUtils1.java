package cn.sparrowmini.common.util;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import java.lang.reflect.*;
import java.util.*;

public class JpaUtils1 {

    public static <T> CriteriaQuery<T> buildQuery(EntityManager em, Class<?> entityClass, Class<T> projectionClass) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(projectionClass);
        Root<?> root = query.from(entityClass);

        List<Selection<?>> selections = buildSelections(cb, root, projectionClass, "", new HashMap<>());

        query.select(cb.construct(projectionClass, selections.toArray(new Selection[0])));

        return query;
    }

    private static List<Selection<?>> buildSelections(CriteriaBuilder cb, From<?, ?> root,
                                                      Class<?> projectionClass, String prefix,
                                                      Map<String, From<?, ?>> joins) {

        List<Selection<?>> selections = new ArrayList<>();

        for (Field field : projectionClass.getDeclaredFields()) {
            String fieldName = field.getName();
            Class<?> fieldType = field.getType();

            if (isSimpleType(fieldType)) {
                // 简单类型直接选字段
                selections.add(root.get(prefix + fieldName).alias(prefix + fieldName));
            } else {
                // 复杂类型需要join
                String joinPath = prefix + fieldName;
                From<?, ?> join = joins.get(joinPath);
                if (join == null) {
                    join = root.join(fieldName, JoinType.LEFT);
                    joins.put(joinPath, join);
                }
                // 递归处理子属性，prefix清空因为join已经定位了路径
                selections.addAll(buildSelections(cb, join, fieldType, "", joins));
            }
        }
        return selections;
    }

    private static boolean isSimpleType(Class<?> clazz) {
        return clazz.isPrimitive()
                || clazz.equals(String.class)
                || Number.class.isAssignableFrom(clazz)
                || Date.class.isAssignableFrom(clazz)
                || clazz.isEnum()
                || clazz.equals(java.time.OffsetDateTime.class);
    }
}