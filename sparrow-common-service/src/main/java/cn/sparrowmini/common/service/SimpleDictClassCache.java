package cn.sparrowmini.common.service;
import cn.sparrowmini.common.model.SimpleDict;
import jakarta.annotation.PostConstruct;
import org.hibernate.SessionFactory;
import org.hibernate.metamodel.spi.MetamodelImplementor;
import org.hibernate.persister.entity.EntityPersister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class SimpleDictClassCache {

    @Autowired
    private SessionFactory sessionFactory;

    // 缓存 Map: Key 是 DiscriminatorValue, Value 是对应的 Class 对象
    private Map<String, Class<? extends SimpleDict>> cache = new HashMap<>();

    @PostConstruct
    public void init() {
        MetamodelImplementor metamodel = (MetamodelImplementor) sessionFactory.getMetamodel();

        // 遍历 Hibernate 管理的所有实体
        for (EntityPersister persister : metamodel.entityPersisters().values()) {
            Class<?> mappedClass = persister.getMappedClass();

            // 筛选出 SimpleDict 的子类（排除 SimpleDict 基类本身，如果你愿意也可以包含它）
            if (SimpleDict.class.isAssignableFrom(mappedClass) && !SimpleDict.class.equals(mappedClass)) {
                // 获取该实体配置的 DiscriminatorValue
                Object discriminatorValue = persister.getDiscriminatorValue();

                if (discriminatorValue != null) {
                    cache.put(discriminatorValue.toString(), (Class<? extends SimpleDict>) mappedClass);
                }
            }
        }
    }

    /**
     * 从缓存中获取 Class
     */
    public <T extends SimpleDict> Class<T> getClass(String entityType) {
        return (Class<T>) cache.get(entityType);
    }

    /**
     * 获取只读的完整缓存映射
     */
    public Map<String, Class<? extends SimpleDict>> getAllMappings() {
        return Collections.unmodifiableMap(cache);
    }
}