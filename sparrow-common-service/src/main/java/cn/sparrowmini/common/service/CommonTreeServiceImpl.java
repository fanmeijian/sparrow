package cn.sparrowmini.common.service;

import cn.sparrowmini.common.dto.BaseTreeDto;
import cn.sparrowmini.common.model.ApiResponse;
import cn.sparrowmini.common.model.BaseTree;
import cn.sparrowmini.common.repository.BaseTreeRepository;
import cn.sparrowmini.common.util.JsonUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CommonTreeServiceImpl implements CommonTreeService {

    @Autowired(required = false)
    private List<BaseTreeRepository<? extends BaseTree, ?>> baseTreeRepositories;

    @PostConstruct
    public void init() {
        if (baseTreeRepositories == null) {
            baseTreeRepositories = Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends BaseTree, ID> BaseTreeRepository<T, ID> getByDomainClass(Class<T> domainClass) {
        return (BaseTreeRepository<T, ID>) baseTreeRepositories
                .stream()
                .filter(repo -> repo.domainType().equals(domainClass))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No repository found for " + domainClass));
    }


    @Override
    public <ID> void moveNode(ID currentId, ID nextId, Class<? extends BaseTree> domainClass) {
        BaseTreeRepository<? extends BaseTree, ID> baseTreeRepository = getByDomainClass(domainClass);
        baseTreeRepository.move(currentId, nextId);
    }

    @Override
    public <T extends BaseTree, ID> Page<T> getChildren(ID parentId, Pageable pageable, Class<T> domainClass) {
        BaseTreeRepository<T, ID> baseTreeRepository = getByDomainClass(domainClass);
        return baseTreeRepository.getChildren(parentId,pageable);
    }

    @Override
    public <T extends BaseTree, P extends BaseTreeDto, ID> Page<P> getChildrenProjection(ID parentId, Pageable pageable, Class<T> domainClass, Class<P> projectionClass) {
        BaseTreeRepository<T, ID> baseTreeRepository = getByDomainClass(domainClass);
        return baseTreeRepository.findByParentIdProjection(parentId,pageable, projectionClass);
    }

    @Override
    public <T extends BaseTree, ID> T getNode(ID id, Class<T> domainClass) {
        BaseTreeRepository<T, ID> baseTreeRepository = getByDomainClass(domainClass);
        ID pk = JsonUtils.getMapper().convertValue(id, baseTreeRepository.idType());
        return baseTreeRepository.findById(pk).orElseThrow();
    }

    @Override
    public <T extends BaseTree, ID> ApiResponse<List<ID>> saveNode(List<Map<String, Object>> entitiesMap, Class<T> domainClass) {
        BaseTreeRepository<T, ID> baseTreeRepository = getByDomainClass(domainClass);
        return new ApiResponse<>(baseTreeRepository.upsert(entitiesMap));
    }

    @Override
    public <T extends BaseTree, ID> void deleteNode(Set<ID> ids, Class<T> domainClass) {
        BaseTreeRepository<? extends BaseTree, ID> baseTreeRepository = getByDomainClass(domainClass);
        baseTreeRepository.deleteCascade(ids);
    }

}
