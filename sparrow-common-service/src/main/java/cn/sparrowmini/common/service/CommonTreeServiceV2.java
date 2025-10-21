package cn.sparrowmini.common.service;

import cn.sparrowmini.common.dto.BaseTreeDto;
import cn.sparrowmini.common.model.ApiResponse;
import cn.sparrowmini.common.model.BaseTree;
import cn.sparrowmini.common.model.BaseTreeV2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CommonTreeServiceV2 {

    /**
     * 移动节点
     * @param currentId
     * @param nextId
     */
    public <ID> void moveNode(ID parentId,ID currentId, ID nextId,Class<? extends BaseTreeV2> domainClass);

    /**
     * 获取子节点
     * @param parentId
     * @param pageable
     * @return
     */
    public <T extends BaseTreeV2, ID> Page<T> getChildren(ID parentId, Pageable pageable,Class<T> domainClass, String filter);

    /**
     * 获取子节点
     * @param parentId
     * @param pageable
     * @return
     */
    public <T extends BaseTreeV2, ID> Page<T> getAllChildren(ID parentId, Pageable pageable,Class<T> domainClass, String filter);

    /**
     * 获取子节点
     * @param parentId
     * @param pageable
     * @return
     */
    public <T extends BaseTreeV2,P, ID> Page<P> getAllChildren(ID parentId, Pageable pageable,Class<T> domainClass, String filter, Class<P> projectClass);

    /**
     * 可以获取Projection类
     * @param parentId
     * @param pageable
     * @return
     */
    public <T extends BaseTreeV2, P, ID> Page<P> getChildrenProjection(ID parentId, Pageable pageable, Class<T> domainClass, Class<P> projectionClass);


    /**
     * 节点详情
     * @param id
     * @return
     */
    public <T extends BaseTreeV2, ID> T getNode(ID id,Class<T> domainClass);

    /**
     * 新增或更新节点
     * @param entitiesMap
     * @return
     */
    public <T extends BaseTreeV2, ID> ApiResponse<List<ID>> saveNode(List<Map<String, Object>> entitiesMap,Class<T> domainClass);

    /**
     * 删除节点
     * @param ids
     */
    public <T extends BaseTreeV2, ID> void deleteNode(Set<ID> ids,Class<T> domainClass);

}
