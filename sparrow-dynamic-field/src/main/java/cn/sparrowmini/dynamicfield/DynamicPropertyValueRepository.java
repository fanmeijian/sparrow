package cn.sparrowmini.dynamicfield;

import cn.sparrowmini.common.repository.BaseRepository;
import cn.sparrowmini.dynamicfield.model.DynamicPropertyValue;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface DynamicPropertyValueRepository<T extends DynamicPropertyValue<?,?>,ID> extends BaseRepository<T,ID> {
    void deleteAllByBusinessId(ID businessId);
}
