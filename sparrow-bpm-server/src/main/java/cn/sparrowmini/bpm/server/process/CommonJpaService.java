package cn.sparrowmini.bpm.server.process;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface CommonJpaService {

    public <T, ID> List<ID> upsertEntity(Class<T> clazz, List<Map<String, Object>> entities);

    public <T, ID> long deleteEntity(Class<T> clazz, Collection<ID> ids);

    public <T, ID> T getEntity(Class<T> clazz, ID id);

    public <T> Page<T> getEntityList(Class<T> clazz, Pageable pageable, String filter);
    
    public <T, P> Page<P> getEntityList(Class<T> clazz, Pageable pageable, String filter, Class<P> projectionClass);

    public <T, ID> List<ID> saveEntity(Class<T> clazz,List<T> entities);
}
