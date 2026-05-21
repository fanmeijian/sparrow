package cn.sparrowmini.dynamicfield;

import cn.sparrowmini.common.repository.BaseRepository;
import cn.sparrowmini.dynamicfield.model.DynamicFieldValue;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface DynamicFieldValueRepository<T extends DynamicFieldValue<?,?>,ID> extends BaseRepository<T,ID> {
    void deleteAllByBusinessId(ID businessId);
}
