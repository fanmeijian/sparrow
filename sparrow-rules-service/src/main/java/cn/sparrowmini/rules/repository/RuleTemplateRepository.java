package cn.sparrowmini.rules.repository;

import cn.sparrowmini.common.repository.BaseStateRepository;
import cn.sparrowmini.rules.model.RuleTemplate;

public interface RuleTemplateRepository extends BaseStateRepository<RuleTemplate, String> {
    RuleTemplate findByCode(String code);
}
