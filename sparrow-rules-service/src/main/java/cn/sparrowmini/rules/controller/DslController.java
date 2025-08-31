package cn.sparrowmini.rules.controller;


import cn.sparrowmini.common.service.CommonJpaService;
import cn.sparrowmini.rules.model.Dsl;
import cn.sparrowmini.rules.model.Dslr;
import cn.sparrowmini.rules.util.DSLExtractor;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.drools.drl.parser.lang.dsl.DSLMappingEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "dsls")
@Tag(name = "dsl", description = "规则模板")
public class DslController {
    @Autowired
    private CommonJpaService commonJpaService;

    @GetMapping("/{dslId}/conditions")
    @ResponseBody
    public List<DSLMappingEntry> getConditions(@PathVariable("dslId") String dslId) {
        Dsl dsl = commonJpaService.getEntity(Dsl.class, dslId);
        return DSLExtractor.extractDSL(dsl.getContent());
    }

    @GetMapping("/{dslId}/to-drl")
    @ResponseBody
    public String getDrlString(@PathVariable("dslId") String dslId) {
        Dsl dsl = commonJpaService.getEntity(Dsl.class, dslId);
        List<Dslr> dslrs = commonJpaService.getEntityList(Dslr.class, PageRequest.of(0,Integer.MAX_VALUE),String.format("dslId='%s'",dsl.getId())).getContent();
        return DSLExtractor.getDrl(dsl.getContent(),dslrs);
    }
}
