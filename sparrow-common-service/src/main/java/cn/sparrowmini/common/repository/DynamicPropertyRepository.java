package cn.sparrowmini.common.repository;

import cn.sparrowmini.common.model.dynamic.DynamicProperty;
import cn.sparrowmini.common.model.dynamic.DynamicPropertyId;
import jakarta.persistence.DiscriminatorValue;

public interface DynamicPropertyRepository<T extends DynamicProperty,ID> extends BaseRepository<T,ID>{
    default boolean existsByKey(String key) {
        // 1. 获取当前 Repository 接口定义的具体实体类 T
        Class<T> domainClass = domainType();

        // 2. 从实体类上获取鉴别器值
        DiscriminatorValue dv = domainClass.getAnnotation(DiscriminatorValue.class);
        if (dv == null) {
            throw new IllegalStateException("实体类 " + domainClass.getSimpleName() + " 缺少 @DiscriminatorValue 注解");
        }

        String entityType = dv.value();

        // 3. 构造联合主键进行查询
        // 注意：这里假设 ID 类型是 DynamicPropertyId
        return existsById((ID) new DynamicPropertyId(entityType, key));
    }
}
