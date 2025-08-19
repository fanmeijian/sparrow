package cn.sparrowmini.common.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import jakarta.persistence.PersistenceContext;

import java.lang.reflect.Field;
import java.util.*;

public class UpsertRepositoryImpl<T, ID> implements UpsertRepository<T, ID> {

    @PersistenceContext
    private EntityManager em;

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void upsert(T entity) {
        Class<?> clazz = entity.getClass();
        Object id = getPrimaryKeyValue(entity, clazz);

        T existing = (T) em.find(clazz, id);
        if (existing != null) {
            copyNonIdFields(entity, existing);
            em.merge(existing);
        } else {
            em.persist(entity);
        }
    }

    @Override
    public void upsertAll(Iterable<T> entities) {
        for (T entity : entities) {
            upsert(entity);
        }
    }

    private Object getPrimaryKeyValue(T entity, Class<?> clazz) {
        try {
            for (Field field : getAllFields(clazz)) {
                if (field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(EmbeddedId.class)) {
                    field.setAccessible(true);
                    return field.get(entity);
                }
            }
            throw new RuntimeException("No @Id or @EmbeddedId found in class or superclasses: " + clazz.getName());
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract primary key", e);
        }
    }

    private void copyNonIdFields(T src, T dest) {
        Set<String> pkFieldNames = new HashSet<>();

        for (Field field : getAllFields(src.getClass())) {
            if (field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(EmbeddedId.class)) {
                pkFieldNames.add(field.getName());
            }
        }

        for (Field field : getAllFields(src.getClass())) {
            if (pkFieldNames.contains(field.getName())) continue;

            field.setAccessible(true);
            try {
                Object val = field.get(src);
                field.set(dest, val);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to copy field: " + field.getName(), e);
            }
        }
    }

    /**
     * 获取类及其所有父类的字段（排除Object类）
     */
    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }
        return fields;
    }
}