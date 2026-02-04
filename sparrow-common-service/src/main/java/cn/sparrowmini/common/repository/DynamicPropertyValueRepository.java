package cn.sparrowmini.common.repository;

import cn.sparrowmini.common.model.dynamic.DynamicProperty;
import cn.sparrowmini.common.model.dynamic.DynamicPropertyValue;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface DynamicPropertyValueRepository<T extends DynamicPropertyValue<?,?>,ID> extends BaseRepository<T,ID>{

}
