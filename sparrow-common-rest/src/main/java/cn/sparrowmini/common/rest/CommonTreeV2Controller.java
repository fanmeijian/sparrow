package cn.sparrowmini.common.rest;

import cn.sparrowmini.common.dto.BaseTreeDto;
import cn.sparrowmini.common.model.ApiResponse;
import cn.sparrowmini.common.model.BaseTreeV2;
import cn.sparrowmini.common.service.CommonTreeServiceV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("common-tree-v2")
public class CommonTreeV2Controller {

    @Autowired
    private CommonTreeServiceV2 commonTreeService;

    /**
     * 移动节点
     * @param currentId
     * @param nextId
     */
    @PatchMapping("/move")
    @ResponseBody
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void moveNode(@RequestParam Object currentId,@RequestParam Object nextId,String className, String parentId){
        Class<? extends BaseTreeV2> domainClass = null;
        try {
            domainClass = (Class<? extends BaseTreeV2>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        commonTreeService.moveNode(parentId,currentId,nextId, domainClass);
    }

    /**
     * 获取子节点
     * @param parentId
     * @param pageable
     * @return
     */
    @GetMapping("/children")
    @ResponseBody
    public <T extends BaseTreeV2, P extends BaseTreeDto> Page<?> getChildren(String parentId, Pageable pageable, String className, String projectionClassName){
        Class<T> domainClass = null;
        Class<P> projectionClass = null;
        try {
            domainClass = (Class<T>) Class.forName(className);
            if(projectionClassName!=null){
                projectionClass = (Class<P>) Class.forName(projectionClassName);
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return projectionClass==null
                ? commonTreeService.getChildren(parentId,pageable,domainClass)
                : commonTreeService.getChildrenProjection(parentId,pageable,domainClass, projectionClass);
    }

    /**
     * 节点详情
     * @param id
     * @return
     */
    @GetMapping
    @ResponseBody
    public <T extends BaseTreeV2> T getNode(@RequestParam Object id,String className){
        Class<T> domainClass = null;
        try {
            domainClass = (Class<T>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return commonTreeService.getNode(id,domainClass);
    }

    /**
     * 新增或更新节点
     * @param entitiesMap
     * @return
     */
    @PostMapping()
    @ResponseBody
    @ResponseStatus(code = HttpStatus.CREATED)
    public <T extends BaseTreeV2, ID> ApiResponse<List<ID>> saveNode(@RequestBody List<Map<String, Object>> entitiesMap,String className){
        Class<T> domainClass = null;
        try {
            domainClass = (Class<T>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return commonTreeService.saveNode(entitiesMap,domainClass);
    }

    /**
     * 删除节点
     * @param ids
     */
    @DeleteMapping("")
    @ResponseBody
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public <T extends BaseTreeV2, ID> void deleteNode(@RequestParam("id") Set<ID> ids,String className){
        Class<T> domainClass = null;
        try {
            domainClass = (Class<T>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        commonTreeService.deleteNode(ids, domainClass);
    }
}
