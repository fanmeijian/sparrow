package cn.sparrowmini.common.util;


import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Id;
import org.reflections.ReflectionUtils;

import java.lang.reflect.Field;

public class EntityUtil {
    public static Class<?> getIdType(Class<?> entityClass) {
        for (Field f : ReflectionUtils.getAllFields(entityClass)) {
            if (f.getAnnotation(Id.class) != null || f.getAnnotation(EmbeddedId.class) != null) {
                return f.getType();
            }
        }
        return null;
    }

    public static boolean equalClass(Class<?> entityClass, Object id){
        return id.getClass().getName().equals(entityClass.getName());
    }

}
