package cn.sparrowmini.common.rest;

import cn.sparrowmini.common.model.dynamic.DynamicProperty;
import cn.sparrowmini.common.model.dynamic.DynamicPropertyId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("dynamic-properties")
public class DynamicPropertyController {

    @Autowired
    private DynamicPropertyService dynamicPropertyService;

    @GetMapping("/by-id")
    public DynamicProperty getDynamicProperty(DynamicPropertyId id) {
        return dynamicPropertyService.getDynamicProperty(id);
    }

    @GetMapping
    public Page<DynamicProperty> getDynamicPropertyList(Pageable pageable, String filter) {
        return dynamicPropertyService.getDynamicPropertyList(pageable, filter);
    }

    @PostMapping("/{entityType}")
    public void saveProperty(@PathVariable String entityType, @RequestBody Map<String, Object> dynamicProperty) {
        dynamicPropertyService.saveDynamicProperty(entityType, dynamicProperty);
    }

    @PostMapping("provider-data")
    public Map<String, Object> getPropertyProviderData(@RequestBody List<String> propertyKeys) {
        List<DynamicProperty> properties = (List<DynamicProperty>) dynamicPropertyService.queryDynamicPropertyByKeys(DynamicProperty.class, propertyKeys);
        Map<String, Object> map = new HashMap<>();
        for (DynamicProperty articleProperty : properties) {
            map.put(articleProperty.getPropertyKey(), dynamicPropertyService.getProviderDataValue(articleProperty));
        }
        return map;

    }
}
