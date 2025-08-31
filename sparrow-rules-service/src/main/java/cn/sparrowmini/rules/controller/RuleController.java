package cn.sparrowmini.rules.controller;

import cn.sparrowmini.common.model.ApiResponse;
import cn.sparrowmini.common.service.CommonJpaService;
import cn.sparrowmini.rules.DrlView;
import cn.sparrowmini.rules.model.Drl;
import cn.sparrowmini.rules.model.RuleTemplate;
import cn.sparrowmini.rules.model.RulesModelBean;
import cn.sparrowmini.rules.repository.DrlRepository;
import cn.sparrowmini.rules.service.RuleTemplateService;
import cn.sparrowmini.rules.service.RulesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;



@RestController
@RequestMapping(value = "rules")
@Tag(name = "rule", description = "规则服务")
public class RuleController {

    @Autowired
    private RulesService rulesService;

    @Autowired
    private DrlRepository drlRepository;

    @Autowired
    private RuleTemplateService ruleTemplateService;

    @Autowired
    private CommonJpaService commonJpaService;

    @GetMapping("/drl/{id}")
    @ResponseBody
    @Operation(summary = "drl文件列表")
    public Drl getDrl(@PathVariable String id) {
        return this.commonJpaService.getEntity(Drl.class,id);
    }

    @Transactional
    @GetMapping("/drl")
    @ResponseBody
    @Operation(summary = "drl文件列表")
    public Page<DrlView> getDrlList(Pageable pageable, String filter) {

        return this.drlRepository.findAllProjection(pageable, filter, DrlView.class);
    }

    @PostMapping("/drl")
    @ResponseBody
    @ResponseStatus(code = HttpStatus.CREATED)
    @Operation(summary = "创建DRL")
    public ApiResponse<List<String>> saveDrl(@RequestBody List<Map<String, Object>> drls) {
        return new ApiResponse<>(this.commonJpaService.upsertEntity(Drl.class,drls));
    }

    @PatchMapping("/drl")
    @ResponseBody
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    @Operation(summary = "更新DRL")
    public void updateDrl(@RequestBody List<Map<String, Object>> drls) {
        this.commonJpaService.upsertEntity(Drl.class, drls);
    }

    @DeleteMapping("/drl")
    @ResponseBody
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    @Operation(summary = "删除DRL")
    public void deleteDrl(@RequestParam("drlId") Set<String> drlId) {
        this.commonJpaService.deleteEntity(Drl.class,drlId);
    }

    @GetMapping("/models")
    @ResponseBody
    @Operation(summary = "模型列表")
    public List<RulesModelBean> getModeList(@RequestParam("packages") List<String> packages) {
        return this.rulesService.models(packages);
    }

    @PostMapping("/templates")
    @ResponseBody
    @ResponseStatus(code = HttpStatus.CREATED)
    @Operation(summary = "新建模版")
    public ApiResponse<List<String>> create(@RequestBody List<RuleTemplate> ruleTemplates) {
        return this.ruleTemplateService.create(ruleTemplates);
    }

    @PatchMapping("/templates")
    @ResponseBody
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    @Operation(summary = "调整模版")
    public void adjust(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "文章内容",
                    required = true,
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = RuleTemplate.class)))
            )
            @RequestBody List<Map<String, Object>> ruleTemplates
    ) {
        ruleTemplateService.adjust(ruleTemplates);
    }

    @DeleteMapping("/templates")
    @ResponseBody
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    @Operation(summary = "作废模版")
    public void delete(@RequestParam("id") Set<String> id) {
        ruleTemplateService.delete(id);
    }

    @GetMapping("/templates/{id}")
    @ResponseBody
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "模版详情")
    public RuleTemplate get(@PathVariable String id) {
        return ruleTemplateService.get(id);
    }

    @GetMapping("/templates")
    @ResponseBody
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "模版列表")
    public Page<RuleTemplate> list(Pageable pageable, String filter) {
        return ruleTemplateService.list(pageable, filter);
    }
}
