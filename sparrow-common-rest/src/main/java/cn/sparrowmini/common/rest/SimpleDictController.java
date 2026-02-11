package cn.sparrowmini.common.rest;

import cn.sparrowmini.common.dto.SimpleDictDto;
import cn.sparrowmini.common.model.ApiResponse;
import cn.sparrowmini.common.model.SimpleDict;
import cn.sparrowmini.common.service.SimpleDictClassCache;
import cn.sparrowmini.common.service.SimpleDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("simple-dict")
public class SimpleDictController {

    @Autowired
    private SimpleDictService simpleDictService;

    @GetMapping("/{entityType}/dict/{id}")
    public Object get(@PathVariable String entityType,@PathVariable String id) {
        return simpleDictService.findById(entityType,id).orElseThrow();
    }

    @GetMapping("/dict/{id}")
    public SimpleDict get(@PathVariable String id) {
        return simpleDictService.findById(id);
    }

    @GetMapping("")
    public Page<?> findAll(Pageable pageable, String filter) {
       return simpleDictService.findAllProject(pageable,filter, SimpleDictDto.class);
    }

    @GetMapping("/{entityType}")
    public Page<?> findAll(Pageable pageable, String filter,@PathVariable String entityType) {
        return simpleDictService.findAll(entityType,pageable,filter, SimpleDictDto.class);
    }

    @PostMapping("/delete")
    public void delete(@RequestBody List<String> ids) {
        simpleDictService.deleteByIds(ids);
    }

    @PostMapping("/{entityType}/delete")
    public void delete(@PathVariable String entityType, @RequestBody List<String> ids) {
        simpleDictService.deleteByIds(entityType,ids);
    }

    @PostMapping("/{entityType}")
    public ApiResponse<List<String>> save(@RequestBody List<Map<String, Object>> entities, @PathVariable String entityType) {

        return new ApiResponse<>(simpleDictService.upsert(entityType, entities));
    }
}
