package cn.sparrowmini.rules.service;

import cn.sparrowmini.common.model.ApiResponse;
import cn.sparrowmini.rules.model.RuleTemplate;
import cn.sparrowmini.rules.repository.RuleTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class RuleTemplateServiceImpl implements RuleTemplateService {

    @Autowired
    private RuleTemplateRepository ruleTemplateRepository;

    @Override
    public ApiResponse<List<String>> create(List<RuleTemplate> ruleTemplates) {
        return new ApiResponse<>(this.ruleTemplateRepository.saveAll(ruleTemplates).stream().map(RuleTemplate::getId).toList());
    }

    @Override
    public void adjust(List<Map<String,Object>> ruleTemplates) {
        ruleTemplateRepository.upsert(ruleTemplates);
    }

    @Override
    public void delete(Set<String> ids) {
        this.ruleTemplateRepository.deleteAllById(ids);
    }

    @Override
    public RuleTemplate get(String id) {
        return this.ruleTemplateRepository.findById(id).orElse(null);
    }

    @Override
    public Page<RuleTemplate> list(Pageable pageable, String filter) {
        return this.ruleTemplateRepository.findAll(pageable,filter);
    }
}
