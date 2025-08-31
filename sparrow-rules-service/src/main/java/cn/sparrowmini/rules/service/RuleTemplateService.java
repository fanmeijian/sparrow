package cn.sparrowmini.rules.service;

import cn.sparrowmini.common.model.ApiResponse;
import cn.sparrowmini.rules.model.RuleTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RuleTemplateService {

    public ApiResponse<List<String>> create(List<RuleTemplate> ruleTemplates);

    public void adjust(List<Map<String,Object>> ruleTemplates);

    public void delete(Set<String> id);

    public RuleTemplate get(String id);

    public Page<RuleTemplate> list(Pageable pageable, String filter);
}
