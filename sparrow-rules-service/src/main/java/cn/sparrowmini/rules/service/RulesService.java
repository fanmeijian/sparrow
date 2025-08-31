package cn.sparrowmini.rules.service;

import cn.sparrowmini.rules.model.RulesModelBean;

import java.util.List;
public interface RulesService {
	public List<RulesModelBean> models(List<String> packages);
}
