package cn.sparrowmini.common.rest;

import cn.sparrowmini.common.model.ViewConfig;
import cn.sparrowmini.common.repository.ViewConfigRepository;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("view-config")
public class ViewConfigController {
    @Resource
    ViewConfigRepository viewConfigRepository;

    @GetMapping
    public Page<ViewConfig> viewConfig(Pageable pageable) {
        return viewConfigRepository.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ViewConfig viewConfig(@PathVariable String id) {
        if(viewConfigRepository.existsById(id)){
            return viewConfigRepository.findById(id).get();
        }

        if(viewConfigRepository.existsByCode(id)){
            return viewConfigRepository.findByCode(id).get();
        }else{
            return viewConfigRepository.findByCode("default").get();
        }

    }

    @Transactional
    @PostMapping
    public void saveViewConfig(@RequestBody Map<String,Object> viewConfig) {
        viewConfigRepository.upsert(viewConfig);
    }

    @Transactional
    @DeleteMapping
    public void deleteViewConfig(@RequestParam("id") Set<String> ids) {
        viewConfigRepository.deleteAllById(ids);
    }
}
