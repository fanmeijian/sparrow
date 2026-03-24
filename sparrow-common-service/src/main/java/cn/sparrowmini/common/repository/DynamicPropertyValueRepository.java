package cn.sparrowmini.common.repository;

import cn.sparrowmini.common.model.dynamic.DynamicPropertyValue;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface DynamicPropertyValueRepository<T extends DynamicPropertyValue<?,?>,ID> extends BaseRepository<T,ID>{
    void deleteAllByBusinessId(ID businessId);
}
