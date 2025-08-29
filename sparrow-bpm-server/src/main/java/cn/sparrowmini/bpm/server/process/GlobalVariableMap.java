package cn.sparrowmini.bpm.server.process;

import cn.sparrowmini.bpm.server.process.model.GlobalVariable;
import cn.sparrowmini.bpm.server.repository.VariableMapRepository;
import cn.sparrowmini.bpm.server.util.SpringContextUtil;
import org.jbpm.services.api.RuntimeDataService;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieContext;
import org.kie.api.runtime.KieSession;
import org.kie.server.services.api.KieServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GlobalVariableMap {
    private final Map<String, Object> variables = new HashMap<>();
    private static final Pattern PATTERN = Pattern.compile("#\\{global\\.([a-zA-Z0-9_]+)}");
    private final String containerId;


    public GlobalVariableMap(String containerId) {
        this.containerId = containerId;
    }

    public Object get(String name) {
        VariableMapRepository variableMapRepository = SpringContextUtil.getBean(VariableMapRepository.class);
        GlobalVariable va = variableMapRepository.findByContainerIdAndCode(containerId, name);
        return va.getValue();
    }


    public static String resolve(String text, String containerId) {
        VariableMapRepository variableMapRepository = SpringContextUtil.getBean(VariableMapRepository.class);
        Matcher matcher = PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1); // 取出 xxx
            GlobalVariable va = variableMapRepository.findByContainerIdAndCode(containerId, key);
            String replacement = va == null ? "" : va.getValue();
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}

