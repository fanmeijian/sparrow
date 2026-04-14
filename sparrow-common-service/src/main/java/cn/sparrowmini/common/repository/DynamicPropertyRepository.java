package cn.sparrowmini.common.repository;

import cn.sparrowmini.common.model.dynamic.DynamicProperty;
import cn.sparrowmini.common.model.dynamic.DynamicPropertyId;
import cn.sparrowmini.common.model.dynamic.DynamicProperty_;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DynamicPropertyRepository<T extends DynamicProperty,ID> extends BaseRepository<T,ID>{

    Optional<T> findByPropertyKey(String key);

//    @Query("select s from DynamicProperty s where s.propertyKey in (:keys)")
    default List<T> findByKeys(Collection<String> keys){
        Specification<T>  specification = (root, criteriaQuery, criteriaBuilder) -> {
            return criteriaBuilder.and(root.get(DynamicProperty_.PROPERTY_KEY).in(keys));
        };
        return findAll(specification);
    }

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
