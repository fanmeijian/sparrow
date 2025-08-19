package cn.sparrowmini.common.service;

import cn.sparrowmini.common.model.ApiResponse;
import cn.sparrowmini.common.model.SparrowJpaFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CommonJpaService {

    public <T, ID> List<ID> upsertEntity(Class<T> clazz, List<Map<String, Object>> entities);

    public <T, ID> long deleteEntity(Class<T> clazz, Collection<ID> ids);

    public <T, ID> T getEntity(Class<T> clazz, ID id);

    public <T> Page<T> getEntityList(Class<T> clazz, Pageable pageable, String filter);

    public <T, ID> List<ID> saveEntity(Class<T> clazz,List<T> entities);
}
