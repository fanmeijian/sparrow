package cn.sparrowmini.common.service;

import cn.sparrowmini.common.dto.BaseTreeDto;
import cn.sparrowmini.common.model.ApiResponse;
import cn.sparrowmini.common.model.BaseTree;
import cn.sparrowmini.common.model.BaseTreeV2;
import cn.sparrowmini.common.repository.BaseTreeV2Repository;
import cn.sparrowmini.common.repository.BaseTreeV2Repository;
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
public class CommonTreeServiceV2Impl implements CommonTreeServiceV2 {

    @Autowired(required = false)
    private List<BaseTreeV2Repository<? extends BaseTreeV2, ?>> baseTreeRepositories;

    @PostConstruct
    public void init() {
        if (baseTreeRepositories == null) {
            baseTreeRepositories = Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends BaseTreeV2, ID> BaseTreeV2Repository<T, ID> getByDomainClass(Class<T> domainClass) {
        return (BaseTreeV2Repository<T, ID>) baseTreeRepositories
                .stream()
                .filter(repo -> repo.domainType().equals(domainClass))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No repository found for " + domainClass));
    }


    @Override
    public <ID> void moveNode(ID parentId,ID currentId, ID nextId, Class<? extends BaseTreeV2> domainClass) {
        BaseTreeV2Repository<? extends BaseTreeV2, ID> BaseTreeV2Repository = getByDomainClass(domainClass);
        BaseTreeV2Repository.move(currentId, nextId, (String) parentId);
    }

    @Override
    public <T extends BaseTreeV2, ID> Page<T> getChildren(ID parentId, Pageable pageable, Class<T> domainClass) {
        BaseTreeV2Repository<T, ID> BaseTreeV2Repository = getByDomainClass(domainClass);
        return BaseTreeV2Repository.getChildren(parentId,pageable);
    }

    @Override
    public <T extends BaseTreeV2, P extends BaseTreeDto, ID> Page<P> getChildrenProjection(ID parentId, Pageable pageable, Class<T> domainClass, Class<P> projectionClass) {
        BaseTreeV2Repository<T, ID> BaseTreeV2Repository = getByDomainClass(domainClass);
        return BaseTreeV2Repository.findByParentIdProjection(parentId,pageable, projectionClass);
    }

    @Override
    public <T extends BaseTreeV2, ID> T getNode(ID id, Class<T> domainClass) {
        BaseTreeV2Repository<T, ID> BaseTreeV2Repository = getByDomainClass(domainClass);
        ID pk = JsonUtils.getMapper().convertValue(id, BaseTreeV2Repository.idType());
        return BaseTreeV2Repository.findById(pk).orElseThrow();
    }

    @Override
    public <T extends BaseTreeV2, ID> ApiResponse<List<ID>> saveNode(List<Map<String, Object>> entitiesMap, Class<T> domainClass) {
        BaseTreeV2Repository<T, ID> BaseTreeV2Repository = getByDomainClass(domainClass);
        return new ApiResponse<>(BaseTreeV2Repository.upsert(entitiesMap));
    }

    @Override
    public <T extends BaseTreeV2, ID> void deleteNode(Set<ID> ids, Class<T> domainClass) {
        BaseTreeV2Repository<? extends BaseTreeV2, ID> BaseTreeV2Repository = getByDomainClass(domainClass);
        BaseTreeV2Repository.deleteCascade(ids);
    }

}
