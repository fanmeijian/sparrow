package cn.sparrowmini.common.service;

import cn.sparrowmini.common.model.SimpleDict;
import cn.sparrowmini.common.repository.BaseSimpleDictRepository;
import cn.sparrowmini.common.repository.SimpleDictRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SimpleDictService {

    @Autowired
    private SimpleDictClassCache dictClassCache;

    @Autowired(required = false)
    private List<BaseSimpleDictRepository<? extends SimpleDict>> simpleDictRepositories;

    @Autowired
    private SimpleDictRepository simpleDictRepository;

    @SuppressWarnings("unchecked")
    private <T extends SimpleDict> BaseSimpleDictRepository<T> getRepository(Class<T> clazz) {
        return (BaseSimpleDictRepository<T>)simpleDictRepositories.stream()
                .filter(f -> clazz.equals(f.domainType()))
                .findFirst().orElseThrow();
    }

    @Transactional
    public <T extends SimpleDict> String upsert(Class<T> clazz, Map<String, Object> map) {
        return upsert(clazz,List.of(map)).get(0);
    }

    @Transactional
    public <T extends SimpleDict> String upsert(String discriminator, Map<String, Object> map) {
        return upsert(discriminator,List.of(map)).get(0);
    }

    @Transactional
    public <T extends SimpleDict> List<String> upsert(Class<T> clazz, List<Map<String, Object>> maps) {
        return getRepository(clazz).upsert(maps);
    }

    @Transactional
    public <T extends SimpleDict> List<String> upsert(String discriminator, List<Map<String, Object>> maps) {
        // 从缓存中直接拿 Class，无需反射遍历
        Class<T> clazz = getEntityType(discriminator);
        return upsert(clazz,maps);
    }

    public SimpleDict findById(String id) {
        return simpleDictRepository.findById(id).orElseThrow();
    }

    public <T extends SimpleDict> Optional<T> findById(Class<T> clazz, String id) {
        return getRepository(clazz).findById(id);
    }

    public <T extends SimpleDict> Optional<T> findById(String discriminator, String id) {
        Class<T> clazz = getEntityType(discriminator);
        return findById(clazz,id);
    }


    public <T extends SimpleDict> Page<T> findAll(String discriminator,Pageable pageable, Specification<T> specification) {
        Class<T> clazz = getEntityType(discriminator);
        return findAll(clazz, pageable, specification);
    }

    public <T extends SimpleDict> Page<T> findAll(String discriminator,Pageable pageable, String filter) {
        Class<T> clazz = getEntityType(discriminator);
        BaseSimpleDictRepository<T> repository = getRepository(clazz);
        return repository.findAll(pageable, filter);
    }

    public <T extends SimpleDict> Page<T> findAll(Class<T> clazz,Pageable pageable, String filter) {
        BaseSimpleDictRepository<T> repository = getRepository(clazz);
        return findAll(clazz, pageable, repository.filterSpecification(filter));
    }

    public <T extends SimpleDict> Page<T> findAll(Class<T> clazz,Pageable pageable, Specification<T> specification) {
        return getRepository(clazz).findAll(specification, pageable);
    }

    public Page<SimpleDict> findAll(Pageable pageable, Specification<SimpleDict> specification) {
        return simpleDictRepository.findAll(specification, pageable);
    }

    public <T extends SimpleDict,P> Page<P> findAll(String discriminator,Pageable pageable, String filter, Class<P> pClass) {
        Class<T> clazz = getEntityType(discriminator);
        BaseSimpleDictRepository<T> repository = getRepository(clazz);
        return repository.findAllProjection(pageable, filter, pClass);
    }

    public <P> Page<P> findAllProject(Pageable pageable, String filter, Class<P> clazz) {
        return simpleDictRepository.findAllProjection( pageable, filter, clazz);
    }

    public Page<SimpleDict> findAll(Pageable pageable, String filter) {
        return findAll(pageable,simpleDictRepository.filterSpecification(filter));
    }

    @Transactional
    public <T extends SimpleDict> void deleteById(Class<T> clazz,String id) {
        getRepository(clazz).deleteById(id);
    }

    @Transactional
    public <T extends SimpleDict> void deleteByIds(String discriminator,Collection<String> ids) {
        Class<T> clazz = getEntityType(discriminator);
        deleteByIds(clazz,ids);
    }

    @Transactional
    public <T extends SimpleDict> void deleteByIds(Class<T> clazz,Collection<String> ids) {
        getRepository(clazz).deleteAllById(ids);
    }

    @Transactional
    public void deleteByIds(Collection<String> ids) {
        simpleDictRepository.deleteAllById(ids);
    }

    private <T extends SimpleDict> Class<T> getEntityType(String discriminator) {
        // 从缓存中直接拿 Class，无需反射遍历
        Class<T> clazz = dictClassCache.getClass(discriminator);

        if (clazz == null) {
            throw new IllegalArgumentException("未知的实体类型: " + discriminator);
        }
        return clazz;
    }

}
