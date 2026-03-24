package cn.sparrowmini.common.rest;

import cn.sparrowmini.common.model.dynamic.DynamicProperty;
import cn.sparrowmini.common.service.DynamicPropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("dynamic-properties")
public class DynamicPropertyController {

    @Autowired
    private DynamicPropertyService dynamicPropertyService;

    @GetMapping
    public Page<DynamicProperty> getDynamicPropertyList(Pageable pageable, String filter) {
        return dynamicPropertyService.getDynamicPropertyList(pageable, filter);
    }

    @PostMapping("/{entityType}")
    public void saveProperty(@PathVariable String entityType, @RequestBody Map<String, Object> dynamicProperty) {
        dynamicPropertyService.saveDynamicProperty(entityType, dynamicProperty);
    }
}
