package cn.sparrowmini.common.service;

import cn.sparrowmini.common.dto.ItemVo;
import cn.sparrowmini.common.dto.PropertyVo;

import java.util.List;

/**
 * 分类、属性的服务
 */
public interface CatalogService {

    /**
     * 获取分类下的子类
     * @param classId
     * @return
     */
    public List<ItemVo> getChildrenByClassId(String classId);

    /**
     * 获取分类下的所有属性
     * @param classId
     * @return
     */
    public List<PropertyVo> getPropertiesByClassId(String classId);

    /**
     * 根据codeListId获取到所有的code
     * @param codeListId
     * @return
     */
    public List<ItemVo> getCodeTypeByCodeListId(String codeListId);

    /**
     * 根据类ID获取所有的子类ID
     * @param classId
     * @return
     */
    public List<String> getAllChildrenIdByParentClassId(String classId);


    public List<ItemVo> getOptionsByProperty(String property, String restrictionId);
}
