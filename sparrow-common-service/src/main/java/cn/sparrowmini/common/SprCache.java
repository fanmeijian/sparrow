package cn.sparrowmini.common;

import cn.sparrowmini.common.model.dynamic.DynamicProperty;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.metamodel.EntityType;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component // 建议用 @Component，因为它更像一个工具服务而不是配置类
public class SprCache {

    @PersistenceContext // 注入 EntityManager 的标准注解
    private EntityManager entityManager;

    // 缓存映射：DiscriminatorValue -> Class
    private final Map<String, Class<?>> typeCache = new ConcurrentHashMap<>();

    /**
     * 在 Spring 容器初始化完成后，自动预热缓存
     */
    @PostConstruct
    public void initCache() {
        for (EntityType<?> entity : entityManager.getMetamodel().getEntities()) {
            Class<?> javaType = entity.getJavaType();

            // 确保不为空且是 DynamicProperty 的子类
            if (javaType != null && DynamicProperty.class.isAssignableFrom(javaType)) {
                DiscriminatorValue dv = javaType.getAnnotation(DiscriminatorValue.class);
                if (dv != null) {
                    typeCache.put(dv.value(), javaType);
                }
            }
        }
    }

    /**
     * 对外提供的查询方法
     */
    public Class<?> getEntityClass(String discriminatorValue) {
        Class<?> clazz = typeCache.get(discriminatorValue);
        if (clazz == null) {
            throw new RuntimeException("未识别的动态属性分类: " + discriminatorValue);
        }
        return clazz;
    }
}